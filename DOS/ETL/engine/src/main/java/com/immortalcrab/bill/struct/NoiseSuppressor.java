package com.immortalcrab.bill.struct;

class NoiseSuppressor {

    public static String normalizeSeal(String partNumber) {
        return partNumber.replaceAll("[^a-zA-Z0-9\\s]+", "");
    }
}
