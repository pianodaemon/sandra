package com.immortalcrab.bill.struct;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
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

        for (String name : syms.keySet()) {
            String key = name;
            System.out.print(key + ": ");
            for (var text : syms.get(name)) {
                System.out.print("[" + text + "]");
            }
        }
    }
}
