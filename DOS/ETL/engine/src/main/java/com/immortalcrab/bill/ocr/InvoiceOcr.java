package com.immortalcrab.bill.ocr;

import com.immortalcrab.bill.struct.ExpCommercial;
import com.immortalcrab.bill.pdf.RenderPngHelper;
import com.immortalcrab.bill.struct.XmlFormater;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.Document;

public class InvoiceOcr {

    public static void main(String[] args) {
        try {
            takeInputFromCli(args);
        } catch (InvoiceOcrException e) {
            e.printStackTrace();
        }
    }

    private static void takeInputFromCli(String[] args) throws InvoiceOcrException {
        var options = new Options()
                .addOption(Option.builder("i")
                        .longOpt("input")
                        .required(true)
                        .hasArg(true)
                        .desc("The export commercial invoice PDF")
                        .build())
                .addOption(Option.builder("o")
                        .longOpt("output")
                        .required(true)
                        .hasArg(true)
                        .desc("The export commercial invoice XML")
                        .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine;
        String pdfFilePath;
        String xmlFilePath;
        try {
            cmdLine = parser.parse(options, args);
            pdfFilePath = cmdLine.getOptionValue('i');
            xmlFilePath = cmdLine.getOptionValue('o');
        } catch (ParseException ex) {
            final String emsg = "Parser cli went mad";
            throw new InvoiceOcrException(emsg, ex, ErrorCodes.INVALID_INPUT_TO_PARSE);
        }

        BillOcr bocr = new BillOcr(RenderPngHelper::transformFromPdf);
        ExpCommercial<Document> invoice = new ExpCommercial<>((distInputStream) -> bocr.fetchSymbols(pdfFilePath, distInputStream), XmlFormater.class);
        XmlFormater.writeXMLToFile(xmlFilePath, invoice.structureData());
    }
}
