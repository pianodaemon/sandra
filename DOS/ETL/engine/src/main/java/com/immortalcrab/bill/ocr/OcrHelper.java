package com.immortalcrab.bill.ocr;

import com.immortalcrab.bill.pdf.RenderPngHelper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.tess4j.TesseractException;

public class OcrHelper {

    public static void main(String[] args) {

        try {
            var bocr = new BillOcr(RenderPngHelper::transformFromPdf);
            String pdfFilePath = "C:\\Users\\Edwin Plauchu\\Downloads\\xxxxx\\907347-PrecioVenta.pdf";
            Map<String, List<String>> syms = bocr.fetchSymbols(pdfFilePath, "distributions.json");
            for (String name : syms.keySet()) {
                String key = name;
                System.out.print(key + ": ");
                for (var text : syms.get(name)) {
                    System.out.print("[" + text + "]");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TesseractException ex) {
            Logger.getLogger(OcrHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
