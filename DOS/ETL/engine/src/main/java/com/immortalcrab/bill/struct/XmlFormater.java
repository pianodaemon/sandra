package com.immortalcrab.bill.struct;

import java.math.BigDecimal;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@AllArgsConstructor
public class XmlFormater {

    @NonNull
    private final String invoiceNum;

    @NonNull
    private final String shipToAddr;

    @NonNull
    private final String foreignCarrier;

    @NonNull
    private final String reference;

    @NonNull
    private final String bultos;

    @NonNull
    private final String seal;

    @NonNull
    private final String conEcoNum;

    @NonNull
    private final List<MerchandiseItem> descriptions;

    @NonNull
    private final List<String> quantities;

    @NonNull
    private final List<String> weights;

    public Document render() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element invoiceElement = doc.createElement("Invoice");
        invoiceElement.setAttribute("invoiceNum", invoiceNum);
        invoiceElement.setAttribute("shipToAddr", shipToAddr);
        invoiceElement.setAttribute("foreignCarrier", foreignCarrier);
        invoiceElement.setAttribute("ref", reference);
        invoiceElement.setAttribute("bultos", bultos);
        invoiceElement.setAttribute("seal", seal);
        invoiceElement.setAttribute("conEcoNum", conEcoNum);
        doc.appendChild(invoiceElement);

        Element merchandiseElement = doc.createElement("Merchandise");
        invoiceElement.appendChild(merchandiseElement);
        for (int idx = 0; idx < descriptions.size(); idx++) {
            var item = descriptions.get(idx);
            Element itemElement = doc.createElement("Item");
            itemElement.setAttribute("partNumber", item.getPartNumber().orElseThrow());
            itemElement.setAttribute("description", item.getDescription().orElseThrow());
            itemElement.setAttribute("quantity", quantities.get(idx));
            BigDecimal kgs = ManeuverHelper.removeCommasFromStrMagnitude(weights.get(idx));
            itemElement.setAttribute("weightAsKilograms", kgs.toPlainString());
            BigDecimal pounds = ManeuverHelper.kgsMagnitude(ManeuverHelper.removeCommasFromStrMagnitude(weights.get(idx)));
            itemElement.setAttribute("weightAsPounds", pounds.toPlainString());
            merchandiseElement.appendChild(itemElement);

            if (!item.getSerialNumbers().isEmpty()) {
                Element serialsElement = doc.createElement("Serials");
                itemElement.appendChild(serialsElement);
                for (var serial : item.getSerialNumbers()) {
                    Element serialElement = doc.createElement("Serial");
                    serialElement.setAttribute("alpha", serial);
                    serialsElement.appendChild(serialElement);
                }
            }
        }
        return doc;
    }
}
