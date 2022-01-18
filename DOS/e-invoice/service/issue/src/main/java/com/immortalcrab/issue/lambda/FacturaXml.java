package com.immortalcrab.issue.lambda;

import com.immortalcrab.issue.engine.CfdiRequest;
import com.immortalcrab.issue.engine.Storage;
import com.immortalcrab.issue.error.FormatError;
import com.immortalcrab.issue.error.StorageError;

public class FacturaXml {

    private final CfdiRequest cfdiReq;
    private final Storage st;

    private FacturaXml(CfdiRequest cfdiReq, Storage st) {
        this.cfdiReq = cfdiReq;
        this.st = st;
    }

    public static String render(CfdiRequest cfdiReq,
            Storage st) throws FormatError, StorageError {

        /*
        Here our xml render implementation
         */
        return "uuid";
    }
}
