package ru.codeportfolio.dao;

import ru.codeportfolio.dto.FileDto;

import java.io.InputStream;
import java.util.List;

public interface FilesRepository {

    // CRUD - files

    void saveFile(String path, InputStream stream, long size, String contentType);

    byte[] getFile(String path);
    FileDto getInfoFile(String path);

    FileDto moveFile(String from, String to);

    void deleteFile(String path);


    //CRUD - folders

    void createFolder(String path);

    List<FileDto> getFolder(String path);
    List<FileDto> getInfoFolder(String path);

    void moveFolder(String from, String to);

    void deleteFolder(String path);







    List<FileDto> search(String query);


}
