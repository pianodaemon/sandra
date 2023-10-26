package com.immortalcrab.bill.ocr;

public final class InvoiceOcrException extends Exception {

    final int errorCode;

    public InvoiceOcrException(String message, ErrorCodes errorCode) {
        super(message);
        this.errorCode = errorCode.getCode();
    }

    public InvoiceOcrException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCodes.UNKNOWN_ISSUE.getCode();
    }

    public InvoiceOcrException(String message, Throwable cause, ErrorCodes errorCode) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
    }

    public int getErrorCode() {
        return errorCode;
    }
}
