package com.immortalcrab.issue.engine;

import com.immortalcrab.issue.error.StorageError;
import java.io.InputStream;

public interface Storage {

    public void upload(final String cType,
            final long len,
            final String fileName,
            InputStream inputStream) throws StorageError;
}
