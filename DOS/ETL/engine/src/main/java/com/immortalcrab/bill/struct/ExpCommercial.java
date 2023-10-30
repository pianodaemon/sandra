package com.immortalcrab.bill.struct;

import com.immortalcrab.bill.ocr.ErrorCodes;
import com.immortalcrab.bill.ocr.InvoiceOcrException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.InvocationTargetException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public final class ExpCommercial<S> {

    private static final int NUM_DIGITS_AFTER_ZIP = 5;
    private static final String DIST_FILE = "export_commercial_invoice.json";
    private static final String SYM_MERC_DESC = "MERC_DESC";
    private static final String SYM_MERC_DESC_PILOT = "MERC_DESC_PILOT";
    private static final String SYM_MERC_WEIGHT = "MERC_WEIGHT";
    private static final String SYM_MERC_QUANTITY = "MERC_QUANTITY";
    private static final String SYM_INVOICE_NUM = "INVOICE_NUM";
    private static final String SYM_BULTOS = "BULTOS";
    private static final String SYM_CON_ECO_NUM = "CON_ECO_NUM";
    private static final String SYM_FOREIGN_CARRIER = "FOREIGN_CARRIER";
    private static final String SYM_REFERENCE = "REFERENCE";
    private static final String SYM_SEAL = "SEAL";
    private static final String SYM_SHIP_TO_ADDR = "SHIP_TO_ADDR";
    private static final String SERIAL_NUMBER_COLON = "Serial Number:";

    private final OutputFormater<S> formater;

    public ExpCommercial(ISymbolProvider symProvider, Class<? extends OutputFormater<S>> formaterClass) throws InvoiceOcrException {
        this(
                setupFormater(
                        ExpCommercial.class.getClassLoader().getResourceAsStream("dists" + "/" + DIST_FILE),
                        symProvider,
                        formaterClass)
        );
    }

    private static <T> OutputFormater<T> setupFormater(
            InputStream distInputStream,
            ISymbolProvider symProvider,
            Class<? extends OutputFormater<T>> formaterClass) throws InvoiceOcrException {
        Map<String, List<String>> syms = symProvider.fetchSymbols(distInputStream);
        Map<String, Object> corrections = new HashMap<>();
        UnaryOperator<String> replaceNewLinesForSpaces = symName -> syms.get(symName).get(0).replace("\n", " ");
        UnaryOperator<String> normalizeSeal = seal -> seal.replaceAll("[^a-zA-Z0-9\\s]+", "");

        log.info("Applying corrections to the symbol buffers");
        corrections.put(SYM_MERC_DESC, parseMercsBuffers(syms.get(SYM_MERC_DESC), syms.get(SYM_MERC_DESC_PILOT)));
        corrections.put(SYM_MERC_WEIGHT, ManeuverHelper.sublistWithoutLast(ManeuverHelper.groomBuffers(syms.get(SYM_MERC_WEIGHT))));
        corrections.put(SYM_MERC_QUANTITY, ManeuverHelper.groomBuffers(syms.get(SYM_MERC_QUANTITY)));
        corrections.put(SYM_SHIP_TO_ADDR, replaceNewLinesForSpaces.apply(SYM_SHIP_TO_ADDR));
        for (String name : new String[]{
            SYM_INVOICE_NUM, SYM_BULTOS, SYM_CON_ECO_NUM, SYM_FOREIGN_CARRIER, SYM_REFERENCE, SYM_SEAL
        }) {
            var buffers = syms.get(name);
            final String firstElement = buffers.get(0);
            corrections.put(name, ManeuverHelper.removeNewLines(firstElement));
        }
        corrections.put(SYM_REFERENCE, makeUpReference((String) corrections.get(SYM_REFERENCE)));
        corrections.put(SYM_SHIP_TO_ADDR, makeUpShipToAddr((String) corrections.get(SYM_SHIP_TO_ADDR)));

        log.info("Applying normalizations as needed");
        corrections.put(SYM_SEAL, normalizeSeal.apply((String) corrections.get(SYM_SEAL)));

        log.info("Proceeding to set up the formater finally");
        try {
            return formaterClass.getConstructor(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    List.class,
                    List.class,
                    List.class
            ).newInstance(
                    corrections.get(SYM_INVOICE_NUM),
                    corrections.get(SYM_SHIP_TO_ADDR),
                    corrections.get(SYM_FOREIGN_CARRIER),
                    corrections.get(SYM_REFERENCE),
                    corrections.get(SYM_BULTOS),
                    corrections.get(SYM_SEAL),
                    corrections.get(SYM_CON_ECO_NUM),
                    corrections.get(SYM_MERC_DESC),
                    corrections.get(SYM_MERC_QUANTITY),
                    corrections.get(SYM_MERC_WEIGHT));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new InvoiceOcrException("The formater has been aborted", ex);
        }
    }

    public void carryStructureOut(String xmlFilePath) throws InvoiceOcrException {
        log.info("Rendering data as structured information");
        formater.renderFeaturingSave(xmlFilePath);
    }

    private enum MItemSM {
        PARTNUM,
        DESCRIPTION,
        SERIAL
    }

    private static List<MerchandiseItem> parseMercsBuffers(List<String> buffers, List<String> primes) throws InvoiceOcrException {
        if (buffers.size() != primes.size()) {
            final String emsg = "Original and Prime buffers must feature equal number of elements";
            throw new InvoiceOcrException(emsg, ErrorCodes.INVALID_INPUT_TO_PARSE);
        }
        var listMercs = new LinkedList<MerchandiseItem>();
        for (int buffIdx = 0; buffIdx < buffers.size(); buffIdx++) {
            // The pages not containing MerchandiseItems are featuring blank buffers
            if (!buffers.get(buffIdx).isBlank() && !primes.get(buffIdx).isBlank()) {
                extractMercsFromBuffer(listMercs, buffers.get(buffIdx), primes.get(buffIdx));
                continue;
            }
            log.warn("We have found a page not containing merchandise items");
        }
        return listMercs;
    }

    private static void extractMercsFromBuffer(List<MerchandiseItem> listMercs, String bufferA, String bufferB) {
        Set<Integer> pilots = ManeuverHelper.seekOutPilots(bufferA, bufferB);
        String[] lines = ManeuverHelper.removeEmpties(bufferA.split("\n"));
        MerchandiseItem merchandise = null;
        MItemSM state = MItemSM.PARTNUM;
        int idx = 0;
        while (idx < lines.length) {
            var lineCorrected = ManeuverHelper.removeNewLines(lines[idx]);
            switch (state) {
                case PARTNUM:
                    merchandise = new MerchandiseItem(lineCorrected, null, new LinkedList<>());
                    state = MItemSM.DESCRIPTION;
                    break;
                case DESCRIPTION:
                    if (pilots.contains(idx)) {
                        state = MItemSM.PARTNUM;
                        listMercs.add(merchandise);
                        continue;
                    }
                    if (lineCorrected.startsWith(SERIAL_NUMBER_COLON)) {
                        state = MItemSM.SERIAL;
                        continue;
                    } else {
                        var desc = merchandise.getDescription().orElse("") + " " + lineCorrected;
                        merchandise.setDescription(desc.trim());
                    }
                    break;
                case SERIAL:
                    if (pilots.contains(idx)) {
                        state = MItemSM.PARTNUM;
                        listMercs.add(merchandise);
                        continue;
                    }
                    var serials = merchandise.getSerialNumbers();
                    serials.add(lineCorrected.replace(SERIAL_NUMBER_COLON, "").trim());
                    merchandise.setSerialNumbers(serials);
                    if (idx == (lines.length - 1)) {
                        listMercs.add(merchandise);
                    }
                    break;
            }
            idx++;
        }
    }

    private static String makeUpShipToAddr(final String shipToAddrChunk) throws InvoiceOcrException {
        // Just allowing 5 digits as ZIP
        var pattern = Pattern.compile("^(.+[zZ][iI][pP]:) (.*)$");
        Matcher m = pattern.matcher(shipToAddrChunk);
        if (m.find()) {
            String zipAlpha = m.group(2);
            StringBuilder zipAlphaSimplified = new StringBuilder();
            for (int i = 0; i < zipAlpha.length() && zipAlphaSimplified.length() < NUM_DIGITS_AFTER_ZIP; i++) {
                char ch = zipAlpha.charAt(i);
                if (!Character.isWhitespace(ch)) {
                    zipAlphaSimplified.append(ch);
                }
            }
            return m.group(1) + " " + zipAlphaSimplified;
        }
        final String emsg = "ship to addr symbol make up has been skipped";
        throw new InvoiceOcrException(emsg, ErrorCodes.INVALID_INPUT_TO_PARSE);
    }

    private static String makeUpReference(String referenceChunk) throws InvoiceOcrException {
        // Just looking for the second set of digits
        var pattern = Pattern.compile("^(.*)\\b([0-9]+)$");
        Matcher m = pattern.matcher(referenceChunk);
        if (m.find()) {
            return m.group(2);
        }
        final String emsg = "reference symbol make up has been skipped";
        throw new InvoiceOcrException(emsg, ErrorCodes.INVALID_INPUT_TO_PARSE);
    }
}
