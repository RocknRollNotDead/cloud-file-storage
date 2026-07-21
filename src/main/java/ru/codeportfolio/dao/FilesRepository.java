package ru.codeportfolio.dao;

import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import ru.codeportfolio.dto.db.FileDownloadDto;
import ru.codeportfolio.dto.db.FileDto;

import java.io.InputStream;
import java.util.List;

public interface FilesRepository {

    // CRUD - files

    void saveFile(String path, InputStream stream, long size, String contentType);

    FileDto getInfoFile(String path);

    FileDto moveFile(String from, String to);

    void deleteFile(String path);


    //CRUD - folders

    void createFolder(String path);

    List<FileDto> getInfoFolder(String path);

    void moveFolder(String from, String to);

    void deleteFolder(String path);


    InputStream getFiles(String objectName);

    Iterable<Result<Item>> getItems(String path);



    boolean isFolderExist(String folderName);


    List<FileDto> search(String query);


    StatObjectResponse getItem(String path);
}
