package com.immortalcrab.bill.struct;

import com.immortalcrab.bill.ocr.InvoiceOcrException;

public interface IOutputFormater<W> {

    public W render() throws InvoiceOcrException;

    public void saveOnStorage(String filePath, W w) throws InvoiceOcrException;

    public default void renderFeaturingSave(String xmlFilePath) throws InvoiceOcrException {
        saveOnStorage(xmlFilePath, render());
    }
}
