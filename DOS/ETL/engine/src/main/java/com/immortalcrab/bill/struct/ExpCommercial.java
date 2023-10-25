package com.immortalcrab.bill.struct;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.tess4j.TesseractException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExpCommercial {

    private static final String DIST_FILE = "export_commercial_invoice.json";

    private final String profileDirPath;
    private final ISymbolProvider symProvider;

    @FunctionalInterface
    public interface ISymbolProvider {

        public Map<String, List<String>> fetchSymbols(String distPath) throws IOException, TesseractException;
    }

    private String resolveDistributionPath() {
        Path rootPath = Paths.get(profileDirPath);
        Path partialPath = Paths.get(DIST_FILE);
        return rootPath.resolve(partialPath).toString();
    }

    public void structureData() throws IOException, TesseractException {
        Map<String, List<String>> syms = symProvider.fetchSymbols(resolveDistributionPath());
        Map<String, Object> corrections = new HashMap<>();
        for (String name : syms.keySet()) {
            var buffers = syms.get(name);
            final String firstElement = buffers.get(0);
            switch (name) {
                case "MERC_DESC":
                    corrections.put(name, parseBufferDesc(buffers));
                    break;
                case "MERC_WEIGHT":
                    // Total weight is not required
                    corrections.put(name, sublistWithoutLast(groomBuffers(buffers)));
                    break;
                case "MERC_QUANTITY":
                    corrections.put(name, groomBuffers(buffers));
                    break;
                case "INVOICE_NUM":
                case "BULTOS":
                case "CON_ECO_NUM":
                case "FOREIGN_CARRIER":
                case "REFERENCE":
                case "SEAL":
                case "SHIP_TO_ADDR":
                    corrections.put(name, removeNewLines(firstElement));
                    break;

                default:
                    throw new IllegalArgumentException("Unexpected symbol " + name);
            }
        }

        for (var key : corrections.keySet()) {
            var p = key + ": " + corrections.get(key).toString();
            System.out.println(p);
        }

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

    private List<Merchandise> parseBufferDesc(List<String> buffers) {
        var particles = new LinkedList<Merchandise>();
        for (var buffer : buffers) {
            String[] lines = removeEmpties(buffer.split("\n"));
            Merchandise m = null;
            for (int idx = 0; idx < lines.length; idx++) {
                if ((idx % 4) == 0) {
                    m = new Merchandise();
                    var pn = removeNewLines(lines[idx]);
                    m.setPartNumber(pn);
                }
                if ((idx % 4) == 1) {
                    var pn = removeNewLines(lines[idx]);
                    m.setDescription(pn);
                }
                if ((idx % 4) == 2) {
                    var pn = removeNewLines(lines[idx]);
                    m.setDescription(m.getDescription() + pn);
                }
                if ((idx % 4) == 3) {
                    var pn = removeNewLines(lines[idx]);
                    if (pn.startsWith("Serial Number:")) {
                        m.setSerialNumber(pn.replace("Serial Number:", ""));
                    } else {
                        m.setDescription(m.getDescription() + pn);
                        m.setSerialNumber("NA");
                    }
                    particles.add(m);
                }
            }

        }
        return particles;
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
