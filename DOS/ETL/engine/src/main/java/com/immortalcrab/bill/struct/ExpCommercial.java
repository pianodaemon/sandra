package com.immortalcrab.bill.struct;

import com.immortalcrab.bill.ocr.ErrorCodes;
import com.immortalcrab.bill.ocr.InvoiceOcrException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.math.BigDecimal;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExpCommercial {

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

    private final String profileDirPath;
    private final ISymbolProvider symProvider;

    @FunctionalInterface
    public interface ISymbolProvider {

        public Map<String, List<String>> fetchSymbols(String distPath) throws InvoiceOcrException;
    }

    private String resolveDistributionPath() {
        Path rootPath = Paths.get(profileDirPath);
        Path partialPath = Paths.get(DIST_FILE);
        return rootPath.resolve(partialPath).toString();
    }

    public Document structureData() throws InvoiceOcrException {
        try {
            Map<String, List<String>> syms = symProvider.fetchSymbols(resolveDistributionPath());
            Map<String, Object> corrections = new HashMap<>();
            corrections.put(SYM_MERC_DESC, parseMercsBuffers(syms.get(SYM_MERC_DESC), syms.get(SYM_MERC_DESC_PILOT)));
            corrections.put(SYM_MERC_WEIGHT, sublistWithoutLast(groomBuffers(syms.get(SYM_MERC_WEIGHT))));
            corrections.put(SYM_MERC_QUANTITY, groomBuffers(syms.get(SYM_MERC_QUANTITY)));
            String[] names = {
                SYM_INVOICE_NUM, SYM_BULTOS, SYM_CON_ECO_NUM,
                SYM_FOREIGN_CARRIER, SYM_REFERENCE, SYM_SEAL, SYM_SHIP_TO_ADDR
            };
            for (String name : names) {
                var buffers = syms.get(name);
                final String firstElement = buffers.get(0);
                corrections.put(name, removeNewLines(firstElement));
            }
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
            throw new InvoiceOcrException("Original and Prime buffers must feature equal size", ErrorCodes.INVALID_INPUT_TO_PARSE);
        }
        var listMercs = new LinkedList<MerchandiseItem>();
        for (int buffIdx = 0; buffIdx < buffers.size(); buffIdx++) {
            extractMercsFromBuffer(listMercs, buffers.get(buffIdx), primes.get(buffIdx));
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
                    if (lineCorrected.startsWith(SERIAL_NUMBER_COLON)) {
                        var serials = merchandise.getSerialNumbers();
                        serials.add(lineCorrected.replace(SERIAL_NUMBER_COLON, "").trim());
                        merchandise.setSerialNumbers(serials);
                        // Special case when there is no more lines to parse 
                        if (idx == (lines.length - 1)) {
                            listMercs.add(merchandise);
                        }
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
