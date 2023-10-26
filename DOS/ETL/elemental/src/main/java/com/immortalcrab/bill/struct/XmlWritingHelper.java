package com.immortalcrab.bill.struct;

import com.immortalcrab.bill.ocr.InvoiceOcrException;
import org.w3c.dom.Document;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.StringWriter;

public class XmlWritingHelper {

    public static StringWriter indentateDocument(final Document doc) throws InvoiceOcrException {
        try {
            TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory.newInstance();
            Transformer transformer;
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new javax.xml.transform.dom.DOMSource(doc);
            StringWriter writer = new java.io.StringWriter();
            StreamResult result = new javax.xml.transform.stream.StreamResult(writer);
            transformer.transform(source, result);
            return writer;
        } catch (TransformerException ex) {
            throw new InvoiceOcrException("", ex);
        }
    }
}
