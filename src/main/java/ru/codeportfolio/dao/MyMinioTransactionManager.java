package ru.codeportfolio.dao;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.springframework.stereotype.Component;
import ru.codeportfolio.dao.func_interfaces.ConsumerThrowing;
import ru.codeportfolio.dao.func_interfaces.FunctionThrowing;
import ru.codeportfolio.exception.DataAccessException;
import ru.codeportfolio.exception.NotFoundException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class MyMinioTransactionManager {

    private final MinioClient minioClient;

    public MyMinioTransactionManager(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public <T> T executeAction(FunctionThrowing<MinioClient, T> action) {


        T result = null;
        try {
            result = action.apply(minioClient);
        } catch (ErrorResponseException e) {
            if ("NoSuchBucket".equals(e.errorResponse().code())) {
                createBucket();
                result = repeatAction(action);
                // повторить действие
            } else if ("NoSuchKey".equals(e.errorResponse().code())){
                throw new NotFoundException("Файл не найден.");
            } else {
                throw new RuntimeException("MinIO вернул ошибку: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }



    public <T> void executeInTransactionWithoutReturn(ConsumerThrowing<MinioClient> action) {
        executeAction(client -> {
            action.apply(client);
            return null;
        });
    }

    private <T> T repeatAction(FunctionThrowing<MinioClient, T> action) {
        T result;
        try {
            result = action.apply(minioClient);
        } catch (Exception ex) {
            throw new DataAccessException(ex);
        }
        return result;
    }

    private void createBucket() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket("user-files").build())) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket("user-files").build());
            } else {
                throw new DataAccessException("Error buckets");
            }
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 XmlParserException | NoSuchAlgorithmException | IOException | ServerException |
                 InvalidResponseException e) {
            throw new DataAccessException("Error buckets");
        }
    }
}
