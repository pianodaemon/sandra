package com.immortalcrab.issue.engine;

import com.immortalcrab.issue.error.CfdiRequestError;
import com.immortalcrab.issue.error.DecodeError;
import com.immortalcrab.issue.error.FormatError;
import com.immortalcrab.issue.error.PipelineError;
import com.immortalcrab.issue.error.StorageError;
import java.io.InputStreamReader;

public class CfdiEngine {

    private StepDecode sdec = null;
    private StepXml sxml = null;

    public static void transform(
            StepDecode sdec, StepXml sxml,
            InputStreamReader reader,
            Storage st) throws DecodeError, CfdiRequestError, PipelineError, StorageError, FormatError {

        String uuid = new CfdiEngine(sdec, sxml).issue(st, reader);

        System.out.println(uuid);
    }

    private CfdiEngine(StepDecode sdec, StepXml sxml) {

        this.sdec = sdec;
        this.sxml = sxml;
    }

    public String issue(Storage st, InputStreamReader reader)
            throws DecodeError, CfdiRequestError, PipelineError, StorageError, FormatError {

        /* First stage of the pipeline
           It stands for decoding what has been read
           from the data origin */
        CfdiRequest cfdiReq = sdec.render(reader);

        // Pipeline.getInstance().getLOGGER().info(cfdiReq.getDs().toString());

        /* Second stage of the pipeline
           It stands for hand craft a valid xml at sat */
        return sxml.render(cfdiReq, st);
    }
}
