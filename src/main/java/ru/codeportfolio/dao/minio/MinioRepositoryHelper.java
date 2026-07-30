package ru.codeportfolio.dao.minio;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;
import ru.codeportfolio.config.minio.MinioProperties;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class MinioRepositoryHelper {
    protected final MyMinioOperationManager manager;
    protected final String bucketName;


    public MinioRepositoryHelper(MyMinioOperationManager manager, MinioProperties properties) {
        this.manager = manager;
        this.bucketName = properties.bucket();
    }


    public Iterable<Result<Item>> getListItems(MinioClient client, String query, boolean recursive) {
        return client.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(query)
                        .recursive(recursive)
                        .build());

    }

    public void removeObject(String path, MinioClient client) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        client.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .build());
    }

    public void copyFile(String from, String to, MinioClient client) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
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

    public StatObjectResponse getStatResponse(String path, MinioClient client) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        return client.statObject(StatObjectArgs.builder()
                .bucket(bucketName)
                .object(path)
                .build());
    }


}
