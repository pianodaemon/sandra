package com.immortalcrab.issue.error;

public class DecodeError extends EngineError {

    public DecodeError(String message) {
        super(message, ErrorCodes.REQUEST_INVALID);
    }

    public DecodeError(String message, Throwable cause) {
        super(message, cause, ErrorCodes.REQUEST_INVALID);
    }
}