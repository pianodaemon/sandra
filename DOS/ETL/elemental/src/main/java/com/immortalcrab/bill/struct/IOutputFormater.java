package com.immortalcrab.bill.struct;

import com.immortalcrab.bill.ocr.InvoiceOcrException;

public interface IOutputFormater<W> {

    public W render() throws InvoiceOcrException;
}
