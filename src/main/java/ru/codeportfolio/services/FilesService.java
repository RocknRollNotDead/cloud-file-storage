package ru.codeportfolio.services;


import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.codeportfolio.dao.FilesRepository;
import ru.codeportfolio.dto.CreateFolderResponseDto;
import ru.codeportfolio.dto.ResourceResponseDto;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class FilesService {

    private final FilesRepository repository;


    public FilesService(FilesRepository repository) {
        this.repository = repository;
    }


    public List<ResourceResponseDto> getFolder(String path) {
        return null;
    }

    public CreateFolderResponseDto createFolder(String path) {
        return null;
    }

    public ResourceResponseDto getInfo(String path) {
        return null;
    }

    public void delete(String path) {
        //определить файл или папка
        repository.deleteFile(path);
    }

    public ResourceResponseDto move(String from, String to) {
        return null;
    }

    public List<ResourceResponseDto> search(String query) {
        return null;
    }

    public List<ResourceResponseDto> upload(String path) {
        return null;
    }

    public void getResource(String path, OutputStream outputStream) {
        // прочитать сессию, узнать юзера
        // по юзеру достать файл

        String userId = "";

        path = getPath(userId, path);

        if (isFolder(path)) {
//            repository.getFolder(path); // todo настроить скачивание папки или файла
            zipFolder(path, outputStream);
        } else {
//            return repository.getFile(path);
            streamFile(path, outputStream);
        }

        //если это папка - завернуть в зип

//        return null;
    }


    private boolean isFolder(String path) {
        return path.charAt(path.length() - 1) == '/';
    }

    private String getPath(String userId, String path) {
        return "%s/%s".formatted(
                getFolderName(userId),
                path
        );
    }

    private String getFolderName(String userId) {
        return "user-%s-files".formatted(userId);
    }

    public void zipFolder(String path, OutputStream outputStream) {
        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {

            Iterable<Result<Item>> objects = repository.getItems(path);

            for (Result<Item> res : objects) {
                Item item = res.get();

                if (item.isDir()) {
                    continue;
                }

                String entryName = item.objectName().substring(path.length()); // относительный путь внутри архива

                try (InputStream fileStream = repository.getFiles(item.objectName())) {

                    zipOut.putNextEntry(new ZipEntry(entryName));
                    fileStream.transferTo(zipOut);
                    zipOut.closeEntry();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка архивации папки: " + path, e);
        }
    }

    public void streamFile(String path, OutputStream outputStream) {
        try {
            StatObjectResponse object = repository.getItem(path);
            try (InputStream fileStream = repository.getFiles(object.object())) {

                fileStream.transferTo(outputStream);
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка выдачи файла: " + path, e);
        }
    }


}
