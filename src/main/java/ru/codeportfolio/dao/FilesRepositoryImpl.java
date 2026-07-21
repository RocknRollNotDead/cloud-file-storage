package ru.codeportfolio.dao;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.codeportfolio.dto.db.FileDownloadDto;
import ru.codeportfolio.dto.db.FileDto;
import ru.codeportfolio.model.TypeFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class FilesRepositoryImpl implements FilesRepository {
    private final MyMinioTransactionManager manager;
    private final String bucketName;

    public FilesRepositoryImpl(MyMinioTransactionManager manager, @Value("${spring.minio.bucket}") String bucketName) {
        this.manager = manager;
        this.bucketName = bucketName;
    }


    @Override
    public void saveFile(String path, InputStream stream, long size, String contentType) {

        manager.executeInTransactionWithoutReturn(client ->
        {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(stream, size, -1)
                            .contentType(contentType)
                            .build());
        });
    }

    @Override
    public FileDto getInfoFile(String path) {
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
    public FileDto moveFile(String from, String to) {
        return manager.executeAction(client ->
        {
            copyFile(from, to, client);

            removeObject(from, client);

            StatObjectResponse response = getStatResponse(to, client);

            return new FileDto(
                    response.object(),
                    response.size(),
                    TypeFile.FILE
            );
        });

    }

    @Override
    public void deleteFile(String path) {
        manager.executeInTransactionWithoutReturn(client ->
        {
            removeObject(path, client);
        });

    }


    public byte[] getFile(String path) {
        return manager.executeAction(client ->
        {
            return client.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()).readAllBytes();

        });
    }


    @Override
    public void createFolder(String path) {
        manager.executeInTransactionWithoutReturn(client -> {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path + "/")
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build());
        });
    }

    public List<FileDownloadDto> getFolder(String path) {
        return manager.executeAction(client -> {
            List<FileDownloadDto> result = new ArrayList<>();

            Iterable<Result<Item>> objects = getListItems(client, path, true);

            for (Result<Item> res : objects) {
                try {
                    Item item = res.get();

                    try (InputStream stream = client.getObject(
                            GetObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(item.objectName())
                                    .build())) {

                        result.add(new FileDownloadDto(item.objectName(), stream.readAllBytes()));
                    }

                } catch (Exception e) {
                    throw new RuntimeException("Ошибка чтения файлов из папки: " + path, e);
                }
            }

            return result;
        });
    }


    public InputStream getFiles(String objectName) {

        return manager.executeAction(client -> {
            return client.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        });
    }

    @Override
    public Iterable<Result<Item>> getItems(String path) {
        return manager.executeAction(client -> {
            return client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(path)
                            .recursive(true)
                            .build());
        });
    }

    @Override
    public StatObjectResponse getItem(String path) {
        return manager.executeAction(client ->
        {
            return getStatResponse(path, client);
        });

    }

    @Override
    public void moveFolder(String from, String to) {
        manager.executeInTransactionWithoutReturn(client ->
        {
            Iterable<Result<Item>> objects = getListItems(client, from, true);

            for (Result<Item> result : objects) {
                String oldName = result.get().objectName();
                String newName = to + oldName.substring(from.length());

                copyFile(oldName, newName, client);

                removeObject(oldName, client);
            }
        });
    }

    @Override
    public void deleteFolder(String path) {
        manager.executeInTransactionWithoutReturn(client ->
        {
            Iterable<Result<Item>> objects = getListItems(client, path, true);

            for (Result<Item> result : objects) {
                String oldName = result.get().objectName();

                removeObject(oldName, client);
            }
        });
    }

    @Override
    public List<FileDto> getInfoFolder(String path) {
        return getFileDtos(path);
    }

    @Override
    public List<FileDto> search(String query) {
        return manager.executeAction(client ->
        {
            List<FileDto> result = new ArrayList<>();

            for (Result<Item> item : getListItems(client, query, true)) {

                result.add(new FileDto(
                        item.get().objectName(),
                        item.get().size(),
                        item.get().isDir() ? TypeFile.DIRECTORY : TypeFile.FILE));
            }
            return result;
        });
    }

    @Override
    public boolean isFolderExist(String folderName) {
        return manager.executeAction(client ->
        {
            if (client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(folderName)
                            .recursive(true)  // false = только прямые "дети", true = вообще всё рекурсивно
                            .build()).iterator().hasNext()) {
                return true;
            } else {
                return false;
            }
        });

    }


    private StatObjectResponse getStatResponse(String path, MinioClient client) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        return client.statObject(StatObjectArgs.builder()
                .bucket(bucketName)
                .object(path)
                .build());
    }

    private List<FileDto> getFileDtos(String path) {
        return manager.executeAction(client ->
        {
            List<FileDto> result = new ArrayList<>();

            for (Result<Item> item : getListItems(client, path, false)) {

                if (item.get().objectName().equals(path)) {
                    continue;
                }
                result.add(new FileDto(
                        item.get().objectName(),
                        item.get().isDir() ? null : item.get().size(),
                        item.get().isDir() ? TypeFile.DIRECTORY : TypeFile.FILE
                ));
            }
            return result;
        });
    }

    private Iterable<Result<Item>> getListItems(MinioClient client, String query, boolean recursive) {
        log.info("query " + query);
        return client.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(query)
                        .recursive(recursive)  // false = только прямые "дети", true = вообще всё рекурсивно
                        .build());

    }


    private void removeObject(String path, MinioClient client) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        client.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .build());
        log.info("delete " + path);
    }

    private void copyFile(String from, String to, MinioClient client) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
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

    public Long getSize(String path) {

        return manager.executeAction(client ->
        {
            Long result = 0L;

            for (Result<Item> item : getListItems(client, path, true)) {

                result = result + item.get().size();
            }


            return result;
        });

    }


}
