package com.immortalcrab.bill.struct;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.tess4j.TesseractException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExpCommercial {

    private static final String DIST_FILE = "export_commercial_invoice.json";
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
        corrections.put("MERC_DESC", parseMercsBuffers(syms.get("MERC_DESC"), syms.get("MERC_DESC_PILOT")));
        corrections.put("MERC_WEIGHT", sublistWithoutLast(groomBuffers(syms.get("MERC_WEIGHT"))));
        corrections.put("MERC_QUANTITY", groomBuffers(syms.get("MERC_QUANTITY")));

        String[] names = {
            SYM_INVOICE_NUM, SYM_BULTOS, SYM_CON_ECO_NUM,
            SYM_FOREIGN_CARRIER, SYM_REFERENCE, SYM_SEAL, SYM_SHIP_TO_ADDR
        };
        for (String name : names) {
            var buffers = syms.get(name);
            final String firstElement = buffers.get(0);
            corrections.put(name, removeNewLines(firstElement));
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

    private static Set<Integer> seekOutPilots(String bufferA, String bufferB) {
        String[] a = removeEmpties(bufferA.split("\n"));
        String[] b = removeEmpties(bufferB.split("\n"));

        Set<Integer> pilots = new LinkedHashSet<Integer>();
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

    private List<Merchandise> parseMercsBuffers(List<String> buffers, List<String> primes) {
        var listMercs = new LinkedList<Merchandise>();
        for (int buffIdx = 0; buffIdx < buffers.size(); buffIdx++) {
            extractMercsFromBuffer(listMercs, buffers.get(buffIdx), primes.get(buffIdx));
        }
        return listMercs;
    }

    private static void extractMercsFromBuffer(List<Merchandise> listMercs, String bufferA, String bufferB) {
        Set<Integer> pilots = seekOutPilots(bufferA, bufferB);
        String[] lines = removeEmpties(bufferA.split("\n"));
        Merchandise m = null;
        Pickup state = Pickup.PARTNUM;
        int idx = 0;
        while (idx < lines.length) {
            var lineCorrected = removeNewLines(lines[idx]);
            switch (state) {
                case PARTNUM:
                    m = new Merchandise();
                    m.setPartNumber(lineCorrected);
                    m.setSerialNumber(new LinkedList<String>());
                    state = Pickup.DESCRIPTION;
                    break;
                case DESCRIPTION:
                    if (pilots.contains(idx)) {
                        state = Pickup.PARTNUM;
                        listMercs.add(m);
                        continue;
                    }
                    if (m.getDescription() == null) {
                        m.setDescription(lineCorrected.trim());
                    } else {
                        if (lineCorrected.startsWith(SERIAL_NUMBER_COLON)) {
                            state = Pickup.SERIAL;
                            continue;
                        } else {
                            m.setDescription(m.getDescription() + lineCorrected);
                        }

                    }
                    break;
                case SERIAL:
                    if (pilots.contains(idx)) {
                        state = Pickup.PARTNUM;
                        listMercs.add(m);
                        continue;
                    }
                    if (lineCorrected.startsWith(SERIAL_NUMBER_COLON)) {
                        var serials = m.getSerialNumber();
                        serials.add(lineCorrected.replace(SERIAL_NUMBER_COLON, "").trim());
                        m.setSerialNumber(serials);
                        // Special case when there is no more lines to parse 
                        if (idx == (lines.length - 1)) {
                            listMercs.add(m);
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