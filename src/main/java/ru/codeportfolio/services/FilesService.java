package ru.codeportfolio.services;


import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.codeportfolio.dao.FilesRepository;
import ru.codeportfolio.dao.TransactionManager;
import ru.codeportfolio.dto.CreateFolderResponseDto;
import ru.codeportfolio.dto.FileDto;
import ru.codeportfolio.dto.ResourceResponseDto;


import java.util.List;

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

    public byte[] getResource(String path) {
        // прочитать сессию, узнать юзера
        // по юзеру достать файл

        String userId = "";

        path =  getPath(userId, path);

        if (isFolder(path)){
            repository.getFolder(path); // todo завернуть в зип
//            return
        }else {
            return repository.getFile(path);
        }

        //если это папка - завернуть в зип

//        return null;
    }


    private boolean isFolder(String path){
        return path.charAt(path.length() - 1) == '/';
    }

    private String getPath(String userId, String path){
        return "%s/%s".formatted(
                getFolderName(userId),
                path
        );
    }

    private String getFolderName(String userId){
        return "user-%s-files".formatted(userId);
    }

}
