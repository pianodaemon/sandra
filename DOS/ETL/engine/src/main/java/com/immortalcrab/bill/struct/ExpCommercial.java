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

    enum Pickup {
        PARTNUM,
        DESCRIPTION,
        SERIAL
    }

    private static int[] seekOutPilots(String bufferA, String bufferB) {
        String[] a = removeEmpties(bufferA.split("\n"));
        String[] b = removeEmpties(bufferB.split("\n"));

        var pilots = new LinkedList<Integer>();
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

        int[] pilotsArray = new int[pilots.size()];

        for (int idx = 0; idx < pilotsArray.length; idx++) {
            pilotsArray[idx] = pilots.get(idx);
        }

        return pilotsArray;
    }

    private List<Merchandise> parseBufferDesc(List<String> buffers) {
        var particles = new LinkedList<Merchandise>();
        for (var buffer : buffers) {
            String[] lines = removeEmpties(buffer.split("\n"));
            Merchandise m = null;
            Pickup state = Pickup.PARTNUM;
            int idx = 0;
            short auxCounter = 0;
            while (idx < lines.length) {
                var lineCorrected = removeNewLines(lines[idx]);
                switch (state) {
                    case PARTNUM:
                        auxCounter = 0;
                        m = new Merchandise();
                        m.setPartNumber(lineCorrected);
                        m.setSerialNumber(new LinkedList<String>());
                        state = Pickup.DESCRIPTION;
                        break;
                    case DESCRIPTION:
                        if (auxCounter == 0) {
                            m.setDescription(lineCorrected);
                        }

                        if (auxCounter == 1) {
                            m.setDescription(m.getDescription() + lineCorrected);
                        }

                        if (auxCounter == 2) {
                            if (lineCorrected.startsWith("Serial Number:")) {
                                state = Pickup.SERIAL;
                                continue;
                            } else {
                                m.setDescription(m.getDescription() + lineCorrected);
                                var serials = m.getSerialNumber();
                                serials.add("NA");
                                m.setSerialNumber(serials);
                            }
                            particles.add(m);
                            state = Pickup.PARTNUM;
                        }

                        auxCounter++;
                        break;
                    case SERIAL:
                        if (lineCorrected.startsWith("Serial Number:")) {
                            var serials = m.getSerialNumber();
                            serials.add(lineCorrected.replace("Serial Number:", ""));
                            m.setSerialNumber(serials);
                            // Special case when there is no more lines to parse 
                            if (idx == (lines.length - 1)) {
                                particles.add(m);
                            }
                        } else {
                            particles.add(m);
                            state = Pickup.PARTNUM;
                            continue;
                        }
                        break;
                }

                idx++;
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
