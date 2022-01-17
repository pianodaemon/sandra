package com.immortalcrab.issue.engine;

import java.util.Map;
import com.immortalcrab.issue.error.CfdiRequestError;

public abstract class CfdiRequest {

    protected Map<String, Object> ds = null;

    public Map<String, Object> getDs() {
        return ds;
    }

    protected abstract Map<String, Object> craftImpt() throws CfdiRequestError;
}
