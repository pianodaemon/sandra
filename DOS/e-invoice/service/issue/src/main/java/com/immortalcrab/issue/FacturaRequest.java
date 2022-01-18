package com.immortalcrab.issue;

import com.immortalcrab.issue.engine.CfdiRequest;
import com.immortalcrab.issue.error.CfdiRequestError;
import com.immortalcrab.issue.error.DecodeError;
import java.io.InputStreamReader;
import java.util.Map;

public class FacturaRequest extends CfdiRequest {

    public static FacturaRequest render(InputStreamReader reader) throws CfdiRequestError, DecodeError {
        /*
        Here our request render implementation
         */
        return null;
    }

    @Override
    protected Map<String, Object> craftImpt() throws CfdiRequestError {

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
