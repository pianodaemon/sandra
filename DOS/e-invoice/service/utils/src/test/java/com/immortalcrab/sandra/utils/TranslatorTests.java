package com.immortalcrab.sandra.utils;

import java.io.File;
import java.io.StringWriter;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.util.Random;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import org.python.util.PythonInterpreter;


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
        int len = 10000;
        var argv = new String[len];

        for (int i = 0; i < len; i++) {
            argv[i] = String.valueOf(getRandomLong(0l, 10000000l));
        }

        try {
            String[] expected = translateIntegerToSpanish(argv).split("\n");
            for (int i = 0; i < expected.length; i++) {
                assertEquals(postprocessing(expected[i]),
                        remueveTildes(NumberToSpanishTranslator.translate(Long.parseLong(argv[i]))));
            }
        } catch (Exception e) {
            assertEquals(true, false);
        }
    }

    static long getRandomLong(long min, long range) {
        var rand = new Random();
        long n = rand.nextLong();
        return min + (n < 0l ? n * -1l : n) % range;
    }

    static String postprocessing(String input) {
        String output = input.trim();
        output = output.replaceAll("doscientos", "docientos");
        output = output.replaceAll("trescientos", "trecientos");
        output = output.replaceAll("  ", " ");
        return output;
    }

    static String translateIntegerToSpanish(String[] argv) throws Exception {

        String resourcesDir = System.getenv("RESOURCES_DIR");
        if (resourcesDir == null) {
            resourcesDir = "/resources";
        }
        var preProps = System.getProperties();
        var postProps = new Properties();
        
        var pyScript = new File(resourcesDir + "/numspatrans.py");
        byte[] bytes = Files.readAllBytes(pyScript.toPath());
        var bais = new ByteArrayInputStream(bytes);
        var out = new StringWriter();

        PythonInterpreter.initialize(preProps, postProps, argv);

        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.setOut(out);
            pyInterp.execfile(bais);
        }
        return out.toString();
    }

    static String remueveTildes(String input) {
        // Cadena de caracteres original a sustituir.
        String original = "áéíóú";
        // Cadena de caracteres ASCII que reemplazarán los originales.
        String ascii = "aeiou";
        String output = input;
        for (int i = 0; i < original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }//for i
        return output;
    }
}
