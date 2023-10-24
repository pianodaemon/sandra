package com.immortalcrab.bill.ocr;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@AllArgsConstructor
public class BillOcr {

    private static final int PAGE_DOES_NOT_MATTER = 0;

    protected final @NonNull
    ITransformer imgTransformer;

    protected final @NonNull
    IBillDistributionProvider distProvider;

    @FunctionalInterface
    public interface ITransformer {

        public String[] transformFromFile(final String outputImageDirectory, File file) throws IOException;
    }

    @FunctionalInterface
    public interface IBillDistributionProvider {

        public BillDistribution obtainFromFile(String distPath) throws IOException;
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
        BillDistribution dist = distProvider.obtainFromFile(distPath);
        String[] paths = imgTransformer.transformFromFile(null, new File(pdfFilePath));

        for (int idx = 0; idx < paths.length; idx++) {
            final int pageNumber = idx + 1;
            for (BillDistribution.Distribution d : dist.getDistributions()) {
                if ((d.getPage() == PAGE_DOES_NOT_MATTER) || d.getPage() == pageNumber) {
                    for (var section : d.getSections()) {
                        final String ocrResult = tesseract.doOCR(new File(paths[idx]), section.getRect());
                        if (syms.containsKey(section.getTitle())) {
                            syms.get(section.getTitle()).add(ocrResult);
                        } else {
                            syms.put(section.getTitle(), new LinkedList<>() {
                                {
                                    add(ocrResult);
                                }
                            });
                        }
                    }
                    break;
                }
            }
        }
        return syms;
    }
}
