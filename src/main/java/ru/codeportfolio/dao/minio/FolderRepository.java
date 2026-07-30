package ru.codeportfolio.dao.minio;

import io.minio.Result;
import io.minio.messages.Item;
import ru.codeportfolio.dto.db.FileDto;

import java.util.List;

public interface FolderRepository {

    //CRUD - folders

    void save(String path);

    List<FileDto> getInfo(String path);

    List<FileDto> search(String query);

    void move(String from, String to);

    void delete(String path);

    // не CRUD
    boolean isExist(String folderName);

    Iterable<Result<Item>> getItemsFiles(String path);

    Long getSize(String path);

}
