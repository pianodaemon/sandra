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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class BillDistribution extends JsonToMapHelper {

    List<Distribution> mdistributions;

    public static BillDistribution obtainFromFile(String distPath) throws IOException {
        return obtainFromFile(new File(distPath));
    }

    public static BillDistribution obtainFromFile(File distFile) throws IOException {
        InputStreamReader distIsr = new InputStreamReader(new FileInputStream(distFile));
        return new BillDistribution(distIsr);
    }

    private BillDistribution(InputStreamReader reader) throws IOException {
        super(JsonToMapHelper.readFromReader(reader));
        loadDistributions();
    }

    private void loadDistributions() {
        mdistributions = new LinkedList<>();
        List<Map<String, Object>> distributions = LegoAssembler.obtainObjFromKey(this.getDs(), "distributions");
        distributions.stream().map(d -> {
            Distribution p = new Distribution();
            p.setPage(LegoAssembler.obtainObjFromKey(d, "page"));
            p.setSections(loadSections(LegoAssembler.obtainObjFromKey(d, "sections")));
            return p;
        }).forEachOrdered(p -> {
            mdistributions.add(p);
        });
    }

    private List<Section> loadSections(List<Map<String, Object>> sections) {
        List<Section> ss = new LinkedList<>();
        try {
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
            }).forEachOrdered(p -> {
                ss.add(p);
            });
        } catch (NoSuchElementException ex) {
            log.error("One or more of section might contain typos");
        }
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
