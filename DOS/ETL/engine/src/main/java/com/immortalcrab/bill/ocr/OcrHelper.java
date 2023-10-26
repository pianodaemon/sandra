package com.immortalcrab.bill.ocr;

import com.immortalcrab.bill.struct.ExpCommercial;
import com.immortalcrab.bill.pdf.RenderPngHelper;
import com.immortalcrab.bill.struct.XmlWritingHelper;

public class OcrHelper {

    public static void main(String[] args) {

        try {
            var bocr = new BillOcr(RenderPngHelper::transformFromPdf);
            String pdfFilePath = "C:\\Users\\Edwin Plauchu\\Downloads\\xxxxx\\907773-PrecioVenta.pdf";
            //String pdfFilePath = "C:\\Users\\Edwin Plauchu\\Downloads\\xxxxx\\908168-PrecioVenta.pdf";
            String profileDirPath = ".";
            ExpCommercial invoice = new ExpCommercial(profileDirPath, (distPath) -> bocr.fetchSymbols(pdfFilePath, distPath));

            System.out.println(XmlWritingHelper.indentateDocument(invoice.structureData()));

        } catch (InvoiceOcrException e) {
            e.printStackTrace();
        }
    }
}
