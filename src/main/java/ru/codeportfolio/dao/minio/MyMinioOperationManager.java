package ru.codeportfolio.dao.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import ru.codeportfolio.dao.func_interfaces.ConsumerThrowing;
import ru.codeportfolio.dao.func_interfaces.FunctionThrowing;
import ru.codeportfolio.exception.DataAccessException;
import ru.codeportfolio.exception.NotFoundException;
import ru.codeportfolio.exception.NotFoundResourceException;
import ru.codeportfolio.exception.ValidationException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class MyMinioOperationManager {

    private final MinioClient minioClient;
    private final String bucketName;

    public MyMinioOperationManager(MinioClient minioClient, @Value("${spring.minio.bucket}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    public <T> T executeAction(FunctionThrowing<MinioClient, T> action) {


        T result = null;
        try {
            result = action.apply(minioClient);
        } catch (ErrorResponseException e) {
            if ("NoSuchBucket".equals(e.errorResponse().code())) {
                createBucket();
                result = repeatAction(action);
            } else if ("NoSuchKey".equals(e.errorResponse().code())) {
                throw new NotFoundException("File not found.");
            } else {
                throw new RuntimeException("MinIO return error: " + e.getMessage(), e);
            }
        } catch (NotFoundResourceException e) {
            throw new NotFoundException("Not found resource.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }


    public void executeInTransactionWithoutReturn(ConsumerThrowing<MinioClient> action) {
        executeAction(client -> {
            action.apply(client);
            return null;
        });
    }

    private <T> T repeatAction(FunctionThrowing<MinioClient, T> action) {
        T result;
        try {
            result = action.apply(minioClient);
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return result;
    }

    private void createBucket() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
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
