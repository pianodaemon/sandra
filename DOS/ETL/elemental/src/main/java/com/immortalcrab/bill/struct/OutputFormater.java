package com.immortalcrab.bill.struct;

import com.immortalcrab.bill.ocr.InvoiceOcrException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public abstract class OutputFormater<W> {
    
    @NonNull
    protected final String invoiceNum;

    @NonNull
    protected final String shipToAddr;

    @NonNull
    protected final String foreignCarrier;

    @NonNull
    protected final String reference;

    @NonNull
    protected final String bultos;

    @NonNull
    protected final String seal;

    @NonNull
    protected final String conEcoNum;

    @NonNull
    protected final List<MerchandiseItem> descriptions;

    @NonNull
    protected final List<String> quantities;

    @NonNull
    protected final List<String> weights;

    public abstract W render() throws InvoiceOcrException;

    public abstract void saveOnStorage(String filePath, W w) throws InvoiceOcrException;

    public void renderFeaturingSave(String xmlFilePath) throws InvoiceOcrException {
        saveOnStorage(xmlFilePath, render());
    }
}
