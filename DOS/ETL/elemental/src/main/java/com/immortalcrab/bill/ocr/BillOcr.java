package com.immortalcrab.bill.ocr;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class BillOcr {

    private static final int PAGE_DOES_NOT_MATTER = 0;

    protected final @NonNull
    ITransformer imgTransformer;

    @FunctionalInterface
    public interface ITransformer {

        public String[] transformFromFile(final String outputImageDirectory, File file) throws IOException;
    }

    private ITesseract setupTesseract() {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("tessdata");
        tesseract.setPageSegMode(11);
        tesseract.setOcrEngineMode(2);

        File directory = new File("tessdata");
        if (!directory.exists()) {
            directory.mkdir();
        }

        return tesseract;
    }

    public Map<String, List<String>> fetchSymbols(String pdfFilePath, String distPath) throws IOException, TesseractException {
        Map<String, List<String>> syms = new HashMap<>();
        ITesseract tesseract = setupTesseract();
        BillDistribution dist = BillDistribution.obtainFromFile(distPath);
        String[] paths = imgTransformer.transformFromFile(null, new File(pdfFilePath));

        for (int idx = 0; idx < paths.length; idx++) {
            final int pageNumber = idx + 1;
            for (BillDistribution.Distribution d : dist.getDistributions()) {
                if ((d.getPage() == PAGE_DOES_NOT_MATTER) || d.getPage() == pageNumber) {
                    for (var section : d.getSections()) {
                        applyOcrToImg(syms, tesseract, paths[idx], section.getRect(), section.getTitle());
                    }
                    break;
                }
            }
        }
        return syms;
    }

    private static void applyOcrToImg(Map<String, List<String>> syms, ITesseract tesseract, final String imgPath, Rectangle rect, final String title) throws TesseractException {
        final String ocrResult = tesseract.doOCR(new File(imgPath), rect);
        if (syms.containsKey(title)) {
            syms.get(title).add(ocrResult);
        } else {
            syms.put(title, new LinkedList<>() {
                {
                    add(ocrResult);
                }
            });
        }
    }
}
