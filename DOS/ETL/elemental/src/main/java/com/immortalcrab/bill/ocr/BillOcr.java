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
    private static final String TESS_DATA_DEFAULT_DIR = "tessdata";

    protected final @NonNull
    ITransformer imgTransformer;

    @FunctionalInterface
    public interface ITransformer {

        public String[] transformFromFile(final String outputImageDirectory, File file) throws IOException;
    }

    private static ITesseract setupTesseract() {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESS_DATA_DEFAULT_DIR);
        tesseract.setPageSegMode(11);
        tesseract.setOcrEngineMode(2);
        File directory = new File(TESS_DATA_DEFAULT_DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }
        return tesseract;
    }

    public Map<String, List<String>> fetchSymbols(String pdfFilePath, String distPath) throws InvoiceOcrException {
        Map<String, List<String>> syms = new HashMap<>();
        ITesseract tesseract = setupTesseract();
        BillDistribution dist;
        String[] paths;
        try {
            dist = BillDistribution.obtainFromFile(distPath);
            paths = imgTransformer.transformFromFile(null, new File(pdfFilePath));
        } catch (IOException ex) {
            final String emsg = "A file resource required to fetch the symbols can not be read or written";
            throw new InvoiceOcrException(emsg, ex, ErrorCodes.UNKNOWN_ISSUE);
        }

        try {
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
        } catch (TesseractException ex) {
            final String emsg = "Ocr library failed when fetching the symbols";
            throw new InvoiceOcrException(emsg, ex, ErrorCodes.OCR_LIBRARY_ISSUE);
        }

    }

    private static void applyOcrToImg(Map<String, List<String>> syms,
            ITesseract tesseract, final String imgPath,
            Rectangle rect, final String title) throws TesseractException {
        final String ocrResult = tesseract.doOCR(new File(imgPath), rect);
        if (syms.containsKey(title)) {
            syms.get(title).add(ocrResult);
        } else {
            var buffers = new LinkedList<String>();
            buffers.add(ocrResult);
            syms.put(title, buffers);
        }
    }
}
