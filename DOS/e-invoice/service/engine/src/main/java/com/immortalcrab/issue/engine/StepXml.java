package com.immortalcrab.issue.engine;


import com.immortalcrab.issue.error.FormatError;
import com.immortalcrab.issue.error.StorageError;

public interface StepXml {

    public String render(CfdiRequest cfdiReq, Storage st) throws FormatError, StorageError;
}
