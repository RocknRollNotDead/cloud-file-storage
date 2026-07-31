package ru.codeportfolio.dao.minio;

import io.minio.ListObjectsArgs;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.stereotype.Repository;
import ru.codeportfolio.config.minio.MinioProperties;
import ru.codeportfolio.dto.db.FileDto;
import ru.codeportfolio.exception.NotFoundResourceException;
import ru.codeportfolio.model.TypeFile;
import ru.codeportfolio.util.FolderUtil;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class FolderRepositoryImpl implements FolderRepository {

    private final MyMinioOperationManager manager;
    private final String bucketName;
    private final MinioRepositoryHelper minioRepositoryHelper;


    public FolderRepositoryImpl(MyMinioOperationManager manager, MinioProperties properties, MinioRepositoryHelper minioRepositoryHelper) {

        this.manager = manager;
        this.bucketName = properties.bucket();
        this.minioRepositoryHelper = minioRepositoryHelper;
    }


    @Override
    public void save(String path) {
        manager.executeInTransactionWithoutReturn(client -> {


            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path + "/")
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build());
        });
    }

    @Override
    public List<FileDto> getInfo(String path) {
        return getFilesDto(path);
    }

    @Override
    public List<FileDto> search(String query, String userFolderName) {
        return manager.executeAction(client ->
        {

            List<FileDto> result = new ArrayList<>();

            for (Result<Item> item : minioRepositoryHelper.getListItems(client, userFolderName, true)) {

                var resource = item.get();

                String[] foldersAndFileName = resource.objectName().split("/");

                String originalName = foldersAndFileName[foldersAndFileName.length - 1];

                if (!originalName.contains(query)){
                    System.out.println(originalName + " query: " + query);
                    continue;
                }

                result.add(new FileDto(
                        resource.objectName(),
                        resource.size(),
                        FolderUtil.isFolder(resource.objectName()) ? TypeFile.DIRECTORY : TypeFile.FILE));
            }
            return result;
        });
    }

    @Override
    public void move(String from, String to) {
        manager.executeInTransactionWithoutReturn(client ->
        {
            Iterable<Result<Item>> objects = minioRepositoryHelper.getListItems(client, from, true);

            if (!objects.iterator().hasNext()) {
                throw new NotFoundResourceException();
            }

            for (Result<Item> result : objects) {
                String oldName = result.get().objectName();
                String newName = to + oldName.substring(from.length());

                minioRepositoryHelper.copyFile(oldName, newName, client);

                minioRepositoryHelper.removeObject(oldName, client);
            }
        });
    }

    @Override
    public void delete(String path) {
        manager.executeInTransactionWithoutReturn(client ->
        {
            Iterable<Result<Item>> objects = minioRepositoryHelper.getListItems(client, path, true);

            for (Result<Item> result : objects) {
                String oldName = result.get().objectName();

                minioRepositoryHelper.removeObject(oldName, client);
            }
        });
    }


    @Override
    public Iterable<Result<Item>> getItemsFiles(String path) {
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
    public boolean isExist(String path) {
        return manager.executeAction(client ->
        {

            Iterable<Result<Item>> results = client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(path)
                            .recursive(true)
                            .build());
            Iterator<Result<Item>> iterator = results.iterator();
            return iterator.hasNext();
        });
    }

    @Override
    public Long getSize(String path) {

        return manager.executeAction(client ->
        {
            Long result = 0L;

            for (Result<Item> item : minioRepositoryHelper.getListItems(client, path, true)) {

                result = result + item.get().size();
            }

            return result;
        });
    }


    private List<FileDto> getFilesDto(String path) {
        return manager.executeAction(client ->
        {
            List<FileDto> result = new ArrayList<>();

            for (Result<Item> item : minioRepositoryHelper.getListItems(client, path, false)) {

                var resource = item.get();
                if (item.get().objectName().equals(path)) {
                    continue;
                }
                result.add(new FileDto(
                        resource.objectName(),
                        resource.isDir() ? null : item.get().size(),
                        resource.isDir() ? TypeFile.DIRECTORY : TypeFile.FILE
                ));
            }
            return result;
        });
    }


}
