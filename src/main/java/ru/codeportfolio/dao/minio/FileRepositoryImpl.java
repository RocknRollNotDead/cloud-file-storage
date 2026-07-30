package ru.codeportfolio.dao.minio;

import io.minio.GetObjectArgs;
import io.minio.PutObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import org.springframework.stereotype.Repository;
import ru.codeportfolio.config.minio.MinioProperties;
import ru.codeportfolio.dto.db.FileDto;
import ru.codeportfolio.exception.DataAccessException;
import ru.codeportfolio.exception.NotFoundResourceException;
import ru.codeportfolio.model.TypeFile;

import java.io.InputStream;

@Repository
public class FileRepositoryImpl implements FileRepository {

    private final MyMinioOperationManager manager;
    private final String bucketName;
    private final MinioRepositoryActionHelper minioRepositoryActionHelper;


    public FileRepositoryImpl(MyMinioOperationManager manager, MinioProperties properties, MinioRepositoryActionHelper minioRepositoryActionHelper) {
        this.bucketName = properties.bucket();
        this.manager = manager;
        this.minioRepositoryActionHelper = minioRepositoryActionHelper;
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

            StatObjectResponse response = minioRepositoryActionHelper.getStatResponse(path, client);
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
            StatObjectResponse response = minioRepositoryActionHelper.getStatResponse(path, client);
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
            minioRepositoryActionHelper.copyFile(from, to, client);

            minioRepositoryActionHelper.removeObject(from, client);

            StatObjectResponse response = minioRepositoryActionHelper.getStatResponse(to, client);

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
            minioRepositoryActionHelper.removeObject(path, client);
        });

    }

    public boolean isExist(String path) {
        return manager.executeAction(client -> {
            try {
                minioRepositoryActionHelper.getStatResponse(path, client);
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
