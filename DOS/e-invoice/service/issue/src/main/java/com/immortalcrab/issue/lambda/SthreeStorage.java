package com.immortalcrab.issue.lambda;

import com.immortalcrab.issue.error.StorageError;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.immortalcrab.issue.engine.Storage;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.io.InputStream;

public class SthreeStorage implements Storage {

    private final AmazonS3 amazonS3;

    static public SthreeStorage configure() throws StorageError {

        if (System.getenv("BUCKET_REGION") == null) {
            throw new StorageError("aws region was not fed");
        }

        if (System.getenv("BUCKET_KEY") == null) {
            throw new StorageError("aws key was not fed");
        }

        if (System.getenv("BUCKET_SECRET") == null) {
            throw new StorageError("aws secret was not fed");
        }

        if (System.getenv("BUCKET_TARGET") == null) {
            throw new StorageError("aws bucket was not fed");
        }

        AWSCredentials awsCredentials = new BasicAWSCredentials(System.getenv("BUCKET_KEY"), System.getenv("BUCKET_SECRET"));

        return new SthreeStorage(AmazonS3ClientBuilder.standard().withRegion(System.getenv("BUCKET_REGION")).withCredentials(
                new AWSStaticCredentialsProvider(awsCredentials)
        ).build());
    }

    private SthreeStorage(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public void upload(
            final String cType,
            final long len,
            final String fileName,
            InputStream inputStream) throws StorageError {

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(cType);
        objectMetadata.setContentLength(len);

        try {
            amazonS3.putObject(System.getenv("BUCKET_TARGET"), fileName, inputStream, objectMetadata);

        } catch (AmazonServiceException ex) {
            ex.printStackTrace();
            throw new StorageError(String.format("Error al subir el archivo %s. ", fileName), ex);
        }
    }
}
