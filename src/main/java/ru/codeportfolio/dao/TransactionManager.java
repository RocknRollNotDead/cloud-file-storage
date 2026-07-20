package ru.codeportfolio.dao;

import io.minio.MinioClient;
import io.minio.errors.*;
import org.hibernate.NonUniqueResultException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Component;
import ru.codeportfolio.dao.func_interfaces.ConsumerThrowing;
import ru.codeportfolio.dao.func_interfaces.FunctionThrowing;
import ru.codeportfolio.exceptions.AlreadyExistException;
import ru.codeportfolio.exceptions.NotFoundException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class TransactionManager {

    private final MinioClient minioClient;

    public TransactionManager(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public <T> T executeAction(FunctionThrowing<MinioClient, T> action){


        T result = null;
        try {
            result = action.apply(minioClient);
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                throw new NotFoundException("Файл не найден.");
            }
            throw new RuntimeException("MinIO вернул ошибку: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;

                //todo обработать тут всё

    }

    public <T> void executeInTransactionWithoutReturn(ConsumerThrowing<MinioClient> action){
        executeAction(client -> {
            action.apply(client);
            return null;
        });
    }
}
