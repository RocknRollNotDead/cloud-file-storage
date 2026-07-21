package ru.codeportfolio.services;


import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.codeportfolio.dao.FilesRepository;
import ru.codeportfolio.dao.UserRepository;
import ru.codeportfolio.dto.CreateFolderResponseDto;
import ru.codeportfolio.dto.ResourceResponseDto;
import ru.codeportfolio.exceptions.DataAccessException;
import ru.codeportfolio.exceptions.NotFoundException;
import ru.codeportfolio.exceptions.ValidationException;
import ru.codeportfolio.util.ResourceMapper;
import ru.codeportfolio.util.Validator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class FilesService {

    private final FilesRepository repository;
    private final UserRepository userRepository;


    public FilesService(FilesRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }


    // папки /directory - 1C, 1R
    public CreateFolderResponseDto createFolder(String path, String username) {
        if(!isFolder(path)){
            throw new ValidationException("This is no folder, this is file " + path);
        }
        path = handleRequestAndReturnPath(path, username);
        repository.createFolder(path);
        ResourceResponseDto resourceDto = ResourceMapper.mapFolder(path);
        return  new CreateFolderResponseDto(resourceDto.path(),
                resourceDto.name(),
                resourceDto.type());
    }


    public List<ResourceResponseDto> getFolder(String path, String username) {

        String oldPath = path;
        path = handleRequestAndReturnPath(path, username);
        log.debug("прошло валидацию");
        if(!isFolder(path)){
            throw new ValidationException("This is no folder, this is file " + path);
        }

        if (repository.isFolderExist(path)){

            return ResourceMapper.mapResourcesInFolder(repository.getInfoFolder(path));
        } else {
            throw new NotFoundException("Folder not found " + oldPath);
        }
    }



    // общее - 1C, 3R, 1U, 1D

    public List<ResourceResponseDto> upload(String path, String username, List<MultipartFile> files) {
        // todo проверить вдруг уже гигабайт
        path = handleRequestAndReturnPath(path, username);
        List<ResourceResponseDto> result = new ArrayList<>();
        for(MultipartFile file : files){
            try{
                repository.saveFile(
                        path,
                        file.getInputStream(),
                        file.getSize(),
                        file.getContentType()
                        );
                result.add(ResourceMapper.mapResource(repository.getInfoFile(path)));
            } catch (IOException e) {
                throw new DataAccessException("Ошибка сохранения файла");
            }
        }
        return result;
    }


    public ResourceResponseDto getInfo(String path, String username) {

        path = handleRequestAndReturnPath(path, username);

        if(!isFolder(path)){
            return ResourceMapper.mapResource(
                    repository.getInfoFile(path)
            );
        }
        if (repository.isFolderExist(path)){

            return ResourceMapper.mapFolder(path);
        } else {
            throw new NotFoundException("Folder not found " + path);
        }
    }

    public void getResource(String path, OutputStream outputStream, String username) {
        path = handleRequestAndReturnPath(path, username);

        if (isFolder(path)) {
            zipFolder(path, outputStream);
        } else {
            streamFile(path, outputStream);
        }
    }

    public List<ResourceResponseDto> search(String query, String username) {

        query = handleRequestAndReturnPath(query, username);

        return ResourceMapper.mapResourcesInFolder(repository.search(query));

    }


    public ResourceResponseDto move(String from, String to, String username) {
        from = handleRequestAndReturnPath(from, username);
        to = handleRequestAndReturnPath(to, username);
        if(isFolder(from) && isFolder(to)){
            repository.moveFolder(from, to);
            return ResourceMapper.mapFolder(to);
        } else if(!isFolder(from) && !isFolder(to)) {
            repository.moveFile(from, to);
            return ResourceMapper.mapResource(repository.getInfoFile(to));
        } else {
            throw new ValidationException("Path 1 and path 2 must be both folders or both files!");
        }

    }


    public void delete(String path, String username) {
        path = handleRequestAndReturnPath(path, username);

        if(isFolder(path)){
            repository.deleteFolder(path);
        } else {
            repository.deleteFile(path);
        }

    }













    private void streamFile(String path, OutputStream outputStream) {
        try {
            StatObjectResponse object = repository.getItem(path);
            try (InputStream fileStream = repository.getFiles(object.object())) {

                fileStream.transferTo(outputStream);
            }

        } catch (Exception e) {
            throw new RuntimeException("Ошибка выдачи файла: " + path, e);
        }
    }

    private boolean isFolder(String path) {
        return path.charAt(path.length() - 1) == '/';
    }

    private String getPath(Long userId, String path) {
        return "%s/%s".formatted(
                getFolderName(userId),
                path
        );
    }

    private String getFolderName(Long userId) {
        return "user-%d-files".formatted(userId);
    }

    private void zipFolder(String path, OutputStream outputStream) {
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

    private void createUserFolder(Long userId){

        String folderName = getFolderName(userId);

        if (!repository.isFolderExist(folderName)){
            repository.createFolder(folderName);
        }

    }

    private String handleRequestAndReturnPath(String path, String username){
        username = Validator.validateUsername(username);
        path = Validator.validatePath(path);

        Long userId = userRepository.findUsersByLogin(username).orElseThrow(() -> new NotFoundException("User not found!")).getId();
        createUserFolder(userId);

        return getPath(userId, path);

    }




}
