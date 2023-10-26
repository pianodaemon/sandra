package com.immortalcrab.bill.ocr;

public enum ErrorCodes {

    SUCCESS(0),
    UNKNOWN_ISSUE(100),
    INVALID_INPUT_TO_PARSE(101),
    OCR_LIBRARY_ISSUE(102),
    XML_RENDER_ISSUE(103);

    protected int code;

    ErrorCodes(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
