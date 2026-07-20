package ru.codeportfolio.dao;

import io.minio.MinioClient;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import ru.codeportfolio.exceptions.AlreadyExistException;

import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class TransactionManager {

    private final MinioClient minioClient;

    public TransactionManager(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public <T> T executeInTransaction(FunctionThrowing<MinioClient, T> action){


            try {
                T result = action.apply(minioClient);
                return result;

                //todo обработать тут всё
            } catch (NonUniqueResultException e) {

                throw new RuntimeException(e);
            } catch (ConstraintViolationException e) {

                throw new AlreadyExistException(e);
            } catch (RuntimeException e) {
                throw new RuntimeException("Database Error", e);
            } catch (Exception e) {
                throw new RuntimeException("Database Error", e);
            }
    }

    public <T> void executeInTransactionWithoutReturn(ConsumerThrowing<MinioClient> action){
        executeInTransaction(client -> {
            action.apply(client);
            return null;
        });
    }
}
