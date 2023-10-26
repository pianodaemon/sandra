package com.immortalcrab.bill.ocr;

import com.immortalcrab.bill.struct.ExpCommercial;
import com.immortalcrab.bill.pdf.RenderPngHelper;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.tess4j.TesseractException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;

public class OcrHelper {

    public static void main(String[] args) {

        try {
            var bocr = new BillOcr(RenderPngHelper::transformFromPdf);
            String pdfFilePath = "C:\\Users\\Edwin Plauchu\\Downloads\\xxxxx\\907773-PrecioVenta.pdf";
            //String pdfFilePath = "C:\\Users\\Edwin Plauchu\\Downloads\\xxxxx\\908168-PrecioVenta.pdf";
            String profileDirPath = ".";
            ExpCommercial invoice = new ExpCommercial(profileDirPath, (distPath) -> bocr.fetchSymbols(pdfFilePath, distPath));

            System.out.println(convertDocumentToString(invoice.structureData()));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TesseractException ex) {
            Logger.getLogger(OcrHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(OcrHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Helper method to convert Document to XML string
    private static String convertDocumentToString(Document doc) {
        try {
            javax.xml.transform.TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();

            // Set the output properties for indentation
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); // Indent with 2 spaces

            javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(doc);
            java.io.StringWriter writer = new java.io.StringWriter();
            javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(writer);
            transformer.transform(source, result);
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
