package com.immortalcrab.issue.engine;

import com.immortalcrab.issue.error.CfdiRequestError;
import com.immortalcrab.issue.error.DecodeError;
import java.io.InputStreamReader;

public interface StepDecode {

    public CfdiRequest render(InputStreamReader inReader) throws CfdiRequestError, DecodeError;
}
