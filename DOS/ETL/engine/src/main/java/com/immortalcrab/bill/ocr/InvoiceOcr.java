package com.immortalcrab.bill.ocr;

import com.immortalcrab.bill.struct.ExpCommercial;
import com.immortalcrab.bill.pdf.RenderPngHelper;
import com.immortalcrab.bill.struct.XmlWritingHelper;
import java.io.StringWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class InvoiceOcr {

    public static void main(String[] args) {
        try {
            System.out.println(takeInputFromCli(args));
        } catch (InvoiceOcrException e) {
            e.printStackTrace();
        }
    }

    private static StringWriter takeInputFromCli(String[] args) throws InvoiceOcrException {
        var options = new Options().addOption(Option.builder("i")
                .longOpt("input")
                .required(true)
                .hasArg(true)
                .desc("The export commercial invoice PDF")
                .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine;
        String pdfFilePath;
        try {
            cmdLine = parser.parse(options, args);
            pdfFilePath = cmdLine.getOptionValue('i');
        } catch (ParseException ex) {
            final String emsg = "Parser cli went mad";
            throw new InvoiceOcrException(emsg, ex, ErrorCodes.INVALID_INPUT_TO_PARSE);
        }

        BillOcr bocr = new BillOcr(RenderPngHelper::transformFromPdf);
        ExpCommercial invoice = new ExpCommercial((distInputStream) -> bocr.fetchSymbols(pdfFilePath, distInputStream));
        return XmlWritingHelper.indentateDocument(invoice.structureData());
    }
}
