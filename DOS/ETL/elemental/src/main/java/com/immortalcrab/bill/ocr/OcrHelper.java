package com.immortalcrab.bill.ocr;

import com.immortalcrab.bill.pdf.RenderPngHelper;
import java.io.File;
import java.io.IOException;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class OcrHelper {

    private static final int PAGE_DOES_NOT_MATTER = 0;

    public static void main(String[] args) {

        String pdfFilePath = "C:/NOMINATOR/908158-PrecioVenta.pdf";

        try {

            BillDistribution dist = BillDistribution.obtainFromFile("distribution.json");
            String[] paths = RenderPngHelper.transformFromPdf(null, new File(pdfFilePath));
            for (int idx = 0; idx < paths.length; idx++) {
                extract(dist, paths[idx], idx + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printSection(ITesseract tesseract, BillDistribution.Distribution dist, final String imagePath) throws TesseractException {
        for (var section : dist.getSections()) {
            String ocrResult = tesseract.doOCR(new File(imagePath), section.getRect());
            System.out.println("Section " + section.getTitle() + " Text:\n" + ocrResult);
        }
    }

    private static void extract(BillDistribution dist, final String imagePath, final int pageNumber) {

        // Set up the Tesseract instance
        ITesseract tesseract = new Tesseract();
        File directory = new File("tessdata");
        if (!directory.exists()) {
            directory.mkdir();
        }
        tesseract.setDatapath("tessdata"); // Set the path to your Tesseract data directory

        try {
            // Perform OCR on the image
            tesseract.setPageSegMode(11);
            tesseract.setOcrEngineMode(2);
            for (BillDistribution.Distribution d : dist.getDistributions()) {
                if ((d.getPage() == PAGE_DOES_NOT_MATTER) || (d.getPage() == pageNumber)) {
                    printSection(tesseract, d, imagePath);
                }
            }
        } catch (TesseractException e) {
            e.printStackTrace();
        }
    }
}
