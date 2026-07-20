package ru.codeportfolio.dao;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.codeportfolio.dto.FileDto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FilesRepositoryImpl implements FilesRepository {
    private final TransactionManager manager;
    private final String bucketName;

    public FilesRepositoryImpl(TransactionManager manager, @Value("${user.bucket}") String bucketName) {
        this.manager = manager;
        this.bucketName = bucketName;
    }



    @Override
    public void deleteFile(String path){
        manager.executeInTransactionWithoutReturn(client ->
        {
            removeObject(path, client);
        });

    }



    @Override
    public FileDto getInfoFile(String path) {
        return manager.executeInTransaction(client ->
        {
            StatObjectResponse response = client.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
            return new FileDto(
                    response.object(),
                    response.size()
            );
        });
    }


    public void saveFilee(String path, byte[] file){
        manager.executeInTransactionWithoutReturn(client ->
        {
            client.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .filename("/path/to/test.png")

                            .build());
        });
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
    public byte[] getFile(String path){
        return manager.executeInTransaction(client ->
        {
            return client.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()).readAllBytes();

        });
    }

    @Override
    public FileDto moveFile(String from, String to) {
        return manager.executeInTransaction(client ->
        {
            copyFile(from, to, client);

            removeObject(from, client);

            StatObjectResponse response = client.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(to)
                    .build());

            return new FileDto(
                    response.object(),
                    response.size()
            );
        });

    }



    @Override
    public void createFolder(String path) {
        manager.executeInTransactionWithoutReturn(client ->
        {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)  // слэш в конце
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build());
        });

    }

    @Override
    public List<FileDto> getFolder(String path) {

        return getFileDtos(path);

    }

    private List<FileDto> getFileDtos(String path) {
        return manager.executeInTransaction(client ->
                {
                    List<FileDto> result = new ArrayList<>();

                    for (Result<Item> item : getListItems(client, path, false)) {

                        result.add( new FileDto(
                                item.get().objectName(),
                                item.get().size()
                                        ));


                    }

                    return result;
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
        return manager.executeInTransaction(client ->
        {
            List<FileDto> result = new ArrayList<>();

            for (Result<Item> item : getListItems(client, query, true)) {

                result.add(new FileDto(
                        item.get().objectName(),
                        item.get().size()));
            }
            return result;
        });
    }



    private Iterable<Result<Item>> getListItems(MinioClient client, String query, boolean recursive) {
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


}
