package com.immortalcrab.bill.ocr;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

import com.immortalcrab.bill.meta.JsonToMapHelper;
import com.immortalcrab.bill.meta.LegoAssembler;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
class BillDistribution extends JsonToMapHelper {

    private final List<Distribution> mdistributions;

    public static BillDistribution obtainFromFile(String distPath) throws InvoiceOcrException {
        return obtainFromFile(new File(distPath));
    }

    public static BillDistribution obtainFromFile(File distFile) throws InvoiceOcrException {
        try {
            return obtainFromInputStream(new FileInputStream(distFile));
        } catch (IOException ex) {
            final String emsg = "Distribution file is not available as needed";
            throw new InvoiceOcrException(emsg, ex);
        }
    }

    public static BillDistribution obtainFromInputStream(InputStream is) throws InvoiceOcrException {
        try {
            return new BillDistribution(new InputStreamReader(is));
        } catch (IOException ex) {
            final String emsg = "Distribution input stream is not available as needed";
            throw new InvoiceOcrException(emsg, ex);
        }
    }

    private BillDistribution(InputStreamReader reader) throws InvoiceOcrException, IOException {
        super(JsonToMapHelper.readFromReader(reader));
        mdistributions = new LinkedList<>();
        try {
            List<Map<String, Object>> distributions = LegoAssembler.obtainObjFromKey(this.getDs(), "distributions");
            distributions.stream().map(d -> {
                Distribution p = new Distribution();
                p.setPage(LegoAssembler.obtainObjFromKey(d, "page"));
                p.setSections(loadSections(LegoAssembler.obtainObjFromKey(d, "sections")));
                return p;
            }).forEachOrdered(mdistributions::add);
        } catch (NoSuchElementException ex) {
            final String emsg = "One or more sections might contain missing elements";
            throw new InvoiceOcrException(emsg, ex);
        }
    }

    private List<Section> loadSections(List<Map<String, Object>> sections) {
        List<Section> ss = new LinkedList<>();
        sections.stream().map(i -> {
            Section p = new Section();
            p.setTitle(LegoAssembler.obtainObjFromKey(i, "title"));

            var rect = new Rectangle(
                    LegoAssembler.obtainObjFromKey(i, "x"),
                    LegoAssembler.obtainObjFromKey(i, "y"),
                    LegoAssembler.obtainObjFromKey(i, "width"),
                    LegoAssembler.obtainObjFromKey(i, "height")
            );
            p.setRect(rect);
            return p;
        }).forEachOrdered(ss::add);
        return ss;
    }

    public List<Distribution> getDistributions() {
        return mdistributions;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Distribution {

        private Integer page;
        private List<Section> sections;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Section {

        private String title;
        private Rectangle rect;
    }
}
