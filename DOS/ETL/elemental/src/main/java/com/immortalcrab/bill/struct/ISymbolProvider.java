package com.immortalcrab.bill.struct;

import com.immortalcrab.bill.ocr.InvoiceOcrException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ISymbolProvider {

    public Map<String, List<String>> fetchSymbols(InputStream is) throws InvoiceOcrException;
}
