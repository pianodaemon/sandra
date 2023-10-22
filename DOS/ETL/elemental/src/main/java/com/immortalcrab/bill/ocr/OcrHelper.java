package com.immortalcrab.bill.ocr;

import com.immortalcrab.bill.pdf.RenderPngHelper;
import java.io.File;
import java.io.IOException;

public class OcrHelper {
    public static void main(String[] args) {
        // Replace with your input PDF file path
        String pdfFilePath = "C:/NOMINATOR/908158-PrecioVenta.pdf";
        // Replace with the output image directory
        //String outputImageDirectory = "outputImages/";

        try {
            RenderPngHelper.transformFromPdf(null, new File(pdfFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
