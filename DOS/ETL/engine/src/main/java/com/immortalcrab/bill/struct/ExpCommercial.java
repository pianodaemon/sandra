package com.immortalcrab.bill.struct;

import com.immortalcrab.bill.ocr.ErrorCodes;
import com.immortalcrab.bill.ocr.InvoiceOcrException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.math.BigDecimal;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class ExpCommercial {

    private static final int NUM_DIGITS_AFTER_ZIP = 5;
    private static final BigDecimal FACTOR_KILOGRAM_TO_POUND = new BigDecimal("2.20462");
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

    private final ISymbolProvider symProvider;

    @FunctionalInterface
    public interface ISymbolProvider {

        public Map<String, List<String>> fetchSymbols(InputStream is) throws InvoiceOcrException;
    }

    public Document structureData() throws InvoiceOcrException {
        try {
            InputStream distInputStream = getClass().getClassLoader().getResourceAsStream("dists" + "/" + DIST_FILE);
            Map<String, List<String>> syms = symProvider.fetchSymbols(distInputStream);
            Map<String, Object> corrections = new HashMap<>();
	    UnaryOperator<String> replaceNewLinesForSpaces = symName -> syms.get(symName).get(0).replace("\n", " ");
            log.info("Applying corrections to the symbol buffers");
            corrections.put(SYM_MERC_DESC, parseMercsBuffers(syms.get(SYM_MERC_DESC), syms.get(SYM_MERC_DESC_PILOT)));
            corrections.put(SYM_MERC_WEIGHT, sublistWithoutLast(groomBuffers(syms.get(SYM_MERC_WEIGHT))));
            corrections.put(SYM_MERC_QUANTITY, groomBuffers(syms.get(SYM_MERC_QUANTITY)));
            corrections.put(SYM_SHIP_TO_ADDR, replaceNewLinesForSpaces.apply(SYM_SHIP_TO_ADDR));
            {
                String[] names = {
                    SYM_INVOICE_NUM, SYM_BULTOS, SYM_CON_ECO_NUM,
                    SYM_FOREIGN_CARRIER, SYM_REFERENCE, SYM_SEAL
                };
                for (String name : names) {
                    var buffers = syms.get(name);
                    final String firstElement = buffers.get(0);
                    corrections.put(name, removeNewLines(firstElement));
                }
            }
            {
                /* Latest reference symbol make up
                   Just looking for the second set of digits */
                var referenceChunk = (String) corrections.get(SYM_REFERENCE);
                var pattern = Pattern.compile("^(.*)\\b([0-9]+)$");
                Matcher m = pattern.matcher(referenceChunk);
                if (m.find()) {
                    corrections.put(SYM_REFERENCE, m.group(2));
                } else {
                    log.warn("reference symbol make up has been skipped");
                }
            }
            {
                /* Latest ship to addr symbol make up
                   Just allowing 5 digits as ZIP */
                var shipToAddrChunk = (String) corrections.get(SYM_SHIP_TO_ADDR);
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
                    corrections.put(SYM_SHIP_TO_ADDR, m.group(1) + " " + zipAlphaSimplified);
                } else {
                    log.warn("ship to addr symbol make up has been skipped");
                }
            }
            log.info("Turning the corrections into structured data");
            return genXmlFromCorrections(corrections);
        } catch (ParserConfigurationException ex) {
            final String emsg = "The xml document containing the symbols can not be rendered";
            throw new InvoiceOcrException(emsg, ex, ErrorCodes.XML_RENDER_ISSUE);
        }
    }

    private static BigDecimal removeCommasFromStrMagnitude(final String numberWithCommas) {
        String numberWithoutCommas = numberWithCommas.replace(",", "");
        return new BigDecimal(numberWithoutCommas);
    }

    private static Document genXmlFromCorrections(Map<String, Object> corrections) throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element invoiceElement = doc.createElement("Invoice");
        invoiceElement.setAttribute("invoiceNum", (String) corrections.get(SYM_INVOICE_NUM));
        invoiceElement.setAttribute("shipToAddr", (String) corrections.get(SYM_SHIP_TO_ADDR));
        invoiceElement.setAttribute("foreignCarrier", (String) corrections.get(SYM_FOREIGN_CARRIER));
        invoiceElement.setAttribute("ref", (String) corrections.get(SYM_REFERENCE));
        invoiceElement.setAttribute("bultos", (String) corrections.get(SYM_BULTOS));
        invoiceElement.setAttribute("seal", (String) corrections.get(SYM_SEAL));
        invoiceElement.setAttribute("conEcoNum", (String) corrections.get(SYM_CON_ECO_NUM));
        doc.appendChild(invoiceElement);

        Element merchandiseElement = doc.createElement("Merchandise");
        invoiceElement.appendChild(merchandiseElement);
        var mercs = (List<MerchandiseItem>) corrections.get(SYM_MERC_DESC);
        var quantity = (List<String>) corrections.get(SYM_MERC_QUANTITY);
        var weights = (List<String>) corrections.get(SYM_MERC_WEIGHT);
        for (int idx = 0; idx < mercs.size(); idx++) {
            var item = mercs.get(idx);
            Element itemElement = doc.createElement("Item");
            itemElement.setAttribute("partNumber", item.getPartNumber().orElseThrow());
            itemElement.setAttribute("description", item.getDescription().orElseThrow());
            itemElement.setAttribute("quantity", quantity.get(idx));
            BigDecimal kgs = removeCommasFromStrMagnitude(weights.get(idx));
            itemElement.setAttribute("weightAsKilograms", kgs.toPlainString());
            BigDecimal pounds = removeCommasFromStrMagnitude(weights.get(idx)).multiply(FACTOR_KILOGRAM_TO_POUND);
            itemElement.setAttribute("weightAsPounds", pounds.toPlainString());
            merchandiseElement.appendChild(itemElement);

            if (!item.getSerialNumbers().isEmpty()) {
                Element serialsElement = doc.createElement("Serials");
                itemElement.appendChild(serialsElement);
                for (var serial : item.getSerialNumbers()) {
                    Element serialElement = doc.createElement("Serial");
                    serialElement.setAttribute("alpha", serial);
                    serialsElement.appendChild(serialElement);
                }
            }
        }
        return doc;
    }

    private static List<String> groomBuffers(List<String> buffers) {
        var particles = new LinkedList<String>();
        buffers.stream().map(buff -> buff.split("\n")).forEachOrdered((String[] tokens) -> {
            for (var tok : tokens) {
                if (tok.isBlank()) {
                    continue;
                }
                particles.add(tok);
            }
        });
        return particles;
    }

    private enum Pickup {
        PARTNUM,
        DESCRIPTION,
        SERIAL
    }

    private static Set<Integer> seekOutPilots(String bufferA, String bufferB) {
        String[] a = removeEmpties(bufferA.split("\n"));
        String[] b = removeEmpties(bufferB.split("\n"));
        Set<Integer> pilots = new LinkedHashSet<>();
        int i = 0;
        int j = 0;
        do {
            if (a[i].equals(b[j])) {
                i++;
            } else {
                pilots.add(i - 1);
            }
            j++;
        } while (j < b.length);
        return pilots;
    }

    private List<MerchandiseItem> parseMercsBuffers(List<String> buffers, List<String> primes) throws InvoiceOcrException {
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
        Set<Integer> pilots = seekOutPilots(bufferA, bufferB);
        String[] lines = removeEmpties(bufferA.split("\n"));
        MerchandiseItem merchandise = null;
        Pickup state = Pickup.PARTNUM;
        int idx = 0;
        while (idx < lines.length) {
            var lineCorrected = removeNewLines(lines[idx]);
            switch (state) {
                case PARTNUM:
                    merchandise = new MerchandiseItem(lineCorrected, null, new LinkedList<>());
                    state = Pickup.DESCRIPTION;
                    break;
                case DESCRIPTION:
                    if (pilots.contains(idx)) {
                        state = Pickup.PARTNUM;
                        listMercs.add(merchandise);
                        continue;
                    }
                    if (lineCorrected.startsWith(SERIAL_NUMBER_COLON)) {
                        state = Pickup.SERIAL;
                        continue;
                    } else {
                        var desc = merchandise.getDescription().orElse("") + " " + lineCorrected;
                        merchandise.setDescription(desc.trim());
                    }
                    break;
                case SERIAL:
                    if (pilots.contains(idx)) {
                        state = Pickup.PARTNUM;
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

    private static String removeNewLines(String buffer) {
        return buffer.replace("\n", "");
    }

    private static <T> List<T> sublistWithoutLast(List<T> originalList) {
        return originalList.subList(0, originalList.size() - 1);
    }

    private static String[] removeEmpties(String[] inputArray) {
        int nonEmptyCount = 0;
        for (String str : inputArray) {
            if (!str.isEmpty() && !str.trim().isEmpty()) {
                nonEmptyCount++;
            }
        }
        String[] resultArray = new String[nonEmptyCount];
        int index = 0;
        for (String str : inputArray) {
            if (!str.isEmpty() && !str.trim().isEmpty()) {
                resultArray[index] = str;
                index++;
            }
        }
        return resultArray;
    }
}
