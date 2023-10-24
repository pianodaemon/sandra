package com.immortalcrab.bill.ocr;

import com.immortalcrab.bill.pdf.RenderPngHelper;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.tess4j.TesseractException;

public class OcrHelper {

    public static void main(String[] args) {

        try {
            var bocr = new BillOcr(RenderPngHelper::transformFromPdf, BillDistribution::obtainFromFile);
            String pdfFilePath = "C:/NOMINATOR/908158-PrecioVenta.pdf";
            Map<String, Object> syms = bocr.fetchSymbols(pdfFilePath, "distributions.json");
            for (String name : syms.keySet()) {
                String key = name.toString();
                String value = syms.get(name).toString();
                System.out.println(key + ": " + value);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TesseractException ex) {
            Logger.getLogger(OcrHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
