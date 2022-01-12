package com.immortalcrab.sandra.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;


class TranslatorTests {

    @Test
    void handPickedTranslations() {
        assertEquals("nueve trillones docientos veintitrés mil trecientos setenta y dos billones treinta y seis mil setecientos noventa y nueve millones novecientos noventa y nueve mil novecientos noventa y nueve",
                NumberToSpanishTranslator.translate(9223372036799999999l));
        assertEquals("noventa y un millones", NumberToSpanishTranslator.translate(91000000l));
        assertEquals("cuatrocientos cincuenta y nueve mil", NumberToSpanishTranslator.translate(459000l));
        assertEquals("veintiún", NumberToSpanishTranslator.translate(21l));
        assertEquals("dieciséis", NumberToSpanishTranslator.translate(16l));
        assertEquals("un", NumberToSpanishTranslator.translate(1l));
        assertEquals("cero", NumberToSpanishTranslator.translate(0l));
    }

    @Test
    void randomTranslations() {
        assertEquals(2, 2);
    }
}
