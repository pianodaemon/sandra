package com.immortalcrab.issue.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.immortalcrab.issue.engine.CfdiEngine;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class IssueRequestStreamHandler implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream inputStream,
            OutputStream outputStream, Context context) throws IOException {

        //It'll be called at here correctly
        // CfdiEngine.transform(FacturaRequest::render, FacturaXml::render, reader, st);;
        outputStream.write(("Hello World ").getBytes());
    }

}
