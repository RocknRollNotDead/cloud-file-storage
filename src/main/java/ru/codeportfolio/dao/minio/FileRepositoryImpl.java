package ru.codeportfolio.dao.minio;

import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.codeportfolio.dto.db.FileDto;
import ru.codeportfolio.exception.DataAccessException;
import ru.codeportfolio.exception.NotFoundResourceException;
import ru.codeportfolio.model.TypeFile;

import java.io.InputStream;

@Repository
public class FileRepositoryImpl extends ResourceRepository implements FileRepository {

    public FileRepositoryImpl(MyMinioTransactionManager manager, @Value("${spring.minio.bucket}") String bucketName) {
        super(manager, bucketName);
    }


    @Override
    public FileDto save(String path, InputStream stream, long size, String contentType) {

        return manager.executeAction(client ->
        {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(stream, size, -1)
                            .contentType(contentType)
                            .build());

            StatObjectResponse response = getStatResponse(path, client);
            return new FileDto(
                    response.object(),
                    response.size(),
                    TypeFile.FILE
            );
        });
    }

    @Override
    public FileDto getInfo(String path) {
        return manager.executeAction(client ->
        {
            StatObjectResponse response = getStatResponse(path, client);
            return new FileDto(
                    response.object(),
                    response.size(),
                    TypeFile.FILE
            );
        });
    }

    @Override
    public FileDto move(String from, String to) {
        return manager.executeAction(client ->
        {
            copyFile(from, to, client);

            removeObject(from, client);

            StatObjectResponse response = getStatResponse(to, client);

            if (response.object().isBlank()) {
                throw new NotFoundResourceException();
            }

            return new FileDto(
                    response.object(),
                    response.size(),
                    TypeFile.FILE
            );
        });

    }

    @Override
    public void delete(String path) {
        manager.executeInTransactionWithoutReturn(client ->
        {
            removeObject(path, client);
        });

    }

    public boolean isExist(String path) {
        return manager.executeAction(client -> {
            try {
                getStatResponse(path, client);
                return true;
            } catch (ErrorResponseException e) {
                if (e.errorResponse().code().equals("NoSuchKey")) {
                    return false;
                }
                throw new DataAccessException(e);
            }
        });
    }

    @Override
    public InputStream getStream(String path) {

        return manager.executeAction(client -> {
            return client.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());
        });
    }

}
