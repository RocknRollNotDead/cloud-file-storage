package ru.codeportfolio.service;


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
import ru.codeportfolio.dto.UsersSizeDto;
import ru.codeportfolio.dto.db.FileDto;
import ru.codeportfolio.exception.*;
import ru.codeportfolio.model.User;
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

    public static final long MAX_SIZE_STORAGE_FOR_ONE_USER = 500_000_000L;
    public static final long MEGABYTE = 1_000_000L;
    private final FilesRepository repository;
    private final UserRepository userRepository;


    public FilesService(FilesRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }


    // папки /directory - 1C, 1R
    public CreateFolderResponseDto createFolder(String path, String username) {
        path = handleRequestAndReturnPath(path, username);
        if (!isFolder(path)) {
            throw new ValidationException("This is no folder, this is file!");
        }
        if (repository.isFolderExist(path)) {
            throw new AlreadyExistException("Folder with this name already exist!");
        }

        if (!isExistParentalFolder(path)) {
            throw new NotFoundException("Parental folder not exist!");
        }

        repository.createFolder(path);
        ResourceResponseDto resourceDto = ResourceMapper.mapFolder(path);
        return new CreateFolderResponseDto(resourceDto.path(),
                resourceDto.name(),
                resourceDto.type());
    }


    public List<ResourceResponseDto> getFolder(String path, String username) {

        path = handleRequestAndReturnPath(path, username);

        if (!isFolder(path)) {
            throw new ValidationException("This is no folder, this is file!");
        }

        if (repository.isFolderExist(path)) {

            return ResourceMapper.mapResourcesInFolder(repository.getInfoFolder(path));
        } else {
            throw new NotFoundException("Folder not found!");
        }
    }


    // общее - 1C, 3R, 1U, 1D

    public List<ResourceResponseDto> upload(String path, String username, List<MultipartFile> files) {
        path = handleRequestAndReturnPath(path, username);
        List<ResourceResponseDto> result = new ArrayList<>();
        log.info("%s upload %d:".formatted(username, files.size()));
        String filePath;

        if (checkMemoryForHasHalfGigabyte(username)) {
            double maxSize = (double) MAX_SIZE_STORAGE_FOR_ONE_USER / MEGABYTE;
            throw new OutOfMemoryException(
                    "You're running low on disk space. Buy yourself a hard drive. Max size your files - %f MB"
                            .formatted(maxSize));
        }

        StringBuilder filesNames = new StringBuilder();

        for (MultipartFile file : files) {
            if (file == null) {
                continue;
            }

            if (checkMemoryForHasHalfGigabyte(username)) {
                throw new ValidationException("Error saving file - not enough space. Was save files: " + filesNames);
            }

            filePath = path + file.getOriginalFilename();

            if (repository.isFileExist(filePath)) {
                throw new AlreadyExistException(
                        "This file %s already exist. The download was interrupted. Was save files: %s"
                                .formatted(file.getOriginalFilename(), filesNames));
            }

            try {
                FileDto fileDto = repository.saveFile(
                        filePath,
                        file.getInputStream(),
                        file.getSize(),
                        file.getContentType()
                );
                log.info(filePath);
                result.add(ResourceMapper.mapResource(fileDto));
                filesNames
                        .append(file.getOriginalFilename())
                        .append("; ");
            } catch (IOException e) {
                throw new DataAccessException("Error to save file");
            }

        }
        return result;
    }


    public ResourceResponseDto getInfo(String path, String username) {

        path = handleRequestAndReturnPath(path, username);

        if (!isFolder(path)) {
            return ResourceMapper.mapResource(
                    repository.getInfoFile(path)
            );
        }
        if (repository.isFolderExist(path)) {

            return ResourceMapper.mapFolder(path);
        } else {
            throw new NotFoundException("Folder not found!");
        }
    }

    public void getResource(String path, OutputStream outputStream, String username) {
        path = handleRequestAndReturnPath(path, username);

        if (isFolder(path)) {
            streamFolderAsZipFile(path, outputStream);
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
        if (isFolder(from) && isFolder(to)) {
            if (repository.isFolderExist(to)) {
                throw new AlreadyExistException("Target folder already exist!");
            }
            repository.moveFolder(from, to);
            return ResourceMapper.mapFolder(to);
        } else if (!isFolder(from) && !isFolder(to)) {
            if (repository.isFileExist(to)) {
                throw new AlreadyExistException("Target file already exist!");
            }
            return ResourceMapper.mapResource(repository.moveFile(from, to));
        } else {
            throw new ValidationException("Path 1 and path 2 must be both folders or both files!");
        }

    }


    public void delete(String path, String username) {
        path = handleRequestAndReturnPath(path, username);

        if (isFolder(path)) {
            if (!repository.isFolderExist(path)) {
                throw new NotFoundException("Folder not found!");
            }
            repository.deleteFolder(path);
        } else {
            if (!repository.isFileExist(path)) {
                throw new NotFoundException("Folder not found!");
            }
            repository.deleteFile(path);
        }

    }

    public void deleteAllUserFilesByUserId(Long id) {
        if (id == null) {
            throw new ValidationException("Not found userId");
        }

        String path = getUserPath(id, "");
        repository.deleteFolder(path);

    }


    public List<UsersSizeDto> getUsers() {
        List<User> users = userRepository.findAll();
        List<UsersSizeDto> result = new ArrayList<>();

        for (User user : users) {
            try {
                result.add(new UsersSizeDto(
                        user.getLogin(),
                        user.getId(),
                        repository.getSize(getFolderName(user.getId()))
                ));
            } catch (RuntimeException e) {
                continue;
            }
        }
        return result;
    }


    private void streamFile(String path, OutputStream outputStream) {
        try {
            StatObjectResponse object = repository.getItem(path);

            try (InputStream fileStream = repository.getFiles(object.object())) {

                fileStream.transferTo(outputStream);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("File output error! ", e);
        }
    }

    private void streamFolderAsZipFile(String path, OutputStream outputStream) {
        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {

            Iterable<Result<Item>> objects = repository.getItemsFiles(path);

            for (Result<Item> res : objects) {
                Item item = res.get();

                if (item.isDir()) {
                    continue;
                }

                String entryName = item.objectName().substring(path.length());

                try (InputStream fileStream = repository.getFiles(item.objectName())) {

                    zipOut.putNextEntry(new ZipEntry(entryName));
                    fileStream.transferTo(zipOut);
                    zipOut.closeEntry();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Archive folder error!", e);
        }
    }



    private boolean isFolder(String path) {
        return path.charAt(path.length() - 1) == '/';
    }

    private boolean isExistParentalFolder(String path) {
        String[] folders = path.split("/");
        String fileName = folders[folders.length - 1];
        String folderParentName = path.substring(0, path.length() - (fileName.length() + 1));


        return repository.isFolderExist(folderParentName);
    }

    private String getUserPath(Long userId, String path) {
        return "%s/%s".formatted(
                getFolderName(userId),
                path
        );
    }

    private String getFolderName(Long userId) {
        return "user-%d-files".formatted(userId);
    }


    private void createUserFolder(Long userId) {

        String folderName = getFolderName(userId);

        if (!repository.isFolderExist(folderName)) {
            repository.createFolder(folderName);
        }

    }

    private String handleRequestAndReturnPath(String path, String username) {
        username = Validator.validateUsername(username);
        path = Validator.validatePath(path);

        Long userId = userRepository.findUsersByLogin(username).orElseThrow(() -> new NotFoundException("User not found!")).getId();
        createUserFolder(userId);

        return getUserPath(userId, path);

    }

    private boolean checkMemoryForHasHalfGigabyte(String username) {
        return repository.getSize(handleRequestAndReturnPath("", username)) > MAX_SIZE_STORAGE_FOR_ONE_USER;
    }

}
