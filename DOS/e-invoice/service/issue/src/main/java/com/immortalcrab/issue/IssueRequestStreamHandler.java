package com.immortalcrab.issue;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IssueRequestStreamHandler implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream inputStream,
            OutputStream outputStream, Context context) throws IOException {

        outputStream.write(("Hello World ").getBytes());
    }
}
