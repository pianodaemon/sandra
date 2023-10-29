package com.immortalcrab.bill.struct;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class ManeuverHelper {

    private static final BigDecimal FACTOR_KILOGRAM_TO_POUND = new BigDecimal("2.20462");

    public static BigDecimal kgsMagnitude(final BigDecimal expectedKgs) {
        return expectedKgs.multiply(FACTOR_KILOGRAM_TO_POUND);
    }

    public static Set<Integer> seekOutPilots(String bufferA, String bufferB) {
        String[] a = ManeuverHelper.removeEmpties(bufferA.split("\n"));
        String[] b = ManeuverHelper.removeEmpties(bufferB.split("\n"));
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

    public static List<String> groomBuffers(List<String> buffers) {
        var particles = new LinkedList<String>();
        buffers.stream().map(buff -> buff.split("\n")).forEachOrdered(tokens -> {
            for (var tok : tokens) {
                if (!tok.isBlank()) {
                    particles.add(tok);
                }
            }
        });
        return particles;
    }

    public static String[] removeEmpties(String[] inputArray) {
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

    public static BigDecimal removeCommasFromStrMagnitude(final String numberWithCommas) {
        String numberWithoutCommas = ManeuverHelper.removeSpaces(numberWithCommas.replace(",", ""));
        return new BigDecimal(numberWithoutCommas);
    }

    public static String removeSpaces(String buffer) {
        return buffer.replaceAll("\\s", "");
    }

    public static String removeNewLines(String buffer) {
        return buffer.replace("\n", "");
    }

    public static <T> List<T> sublistWithoutLast(List<T> originalList) {
        return originalList.subList(0, originalList.size() - 1);
    }
}
