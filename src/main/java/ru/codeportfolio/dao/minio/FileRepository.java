package ru.codeportfolio.dao.minio;

import ru.codeportfolio.dto.db.FileDto;

import java.io.InputStream;

public interface FileRepository {

    // CRUD - files

    FileDto save(String path, InputStream stream, long size, String contentType);

    FileDto getInfo(String path);

    FileDto move(String from, String to);

    void delete(String path);

    // не CRUD

    InputStream getStream(String objectName);

    boolean isExist(String fileName);

}
