package ru.codeportfolio.dao.minio;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public abstract class ResourceRepository {
    protected final MyMinioTransactionManager manager;
    protected final String bucketName;

    public abstract void delete(String path);
    public abstract boolean isExist(String path);





    protected ResourceRepository(MyMinioTransactionManager manager, @Value("spring.minio.bucket") String bucketName) {
        this.manager = manager;
        this.bucketName = bucketName;
    }


    protected Iterable<Result<Item>> getListItems(MinioClient client, String query, boolean recursive) {
        return client.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(query)
                        .recursive(recursive)
                        .build());

    }

    protected void removeObject(String path, MinioClient client) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        client.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .build());
    }

    protected void copyFile(String from, String to, MinioClient client) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        client.copyObject(
                CopyObjectArgs.builder()
                        .bucket(bucketName)
                        .object(to)
                        .source(
                                CopySource.builder()
                                        .bucket(bucketName)
                                        .object(from)
                                        .build())
                        .build());
    }

    protected StatObjectResponse getStatResponse(String path, MinioClient client) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        return client.statObject(StatObjectArgs.builder()
                .bucket(bucketName)
                .object(path)
                .build());
    }


}
