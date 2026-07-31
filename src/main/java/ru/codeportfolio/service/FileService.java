package ru.codeportfolio.service;


import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.codeportfolio.dao.UserRepository;
import ru.codeportfolio.dao.minio.FileRepository;
import ru.codeportfolio.dao.minio.FolderRepository;
import ru.codeportfolio.dto.CreateFolderResponseDto;
import ru.codeportfolio.dto.ResourceResponseDto;
import ru.codeportfolio.dto.UsersSizeDto;
import ru.codeportfolio.dto.db.FileDto;
import ru.codeportfolio.exception.*;
import ru.codeportfolio.model.User;
import ru.codeportfolio.util.FolderUtil;
import ru.codeportfolio.util.MemoryCheckUtil;
import ru.codeportfolio.util.ResourceMapper;
import ru.codeportfolio.util.Validator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FileService {


    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    private final UserRepository userRepository;

    private final ResourceStreamer streamer;

    public FileService(FileRepository fileRepository, FolderRepository foldersRepository, UserRepository userRepository, ResourceStreamer streamer) {
        this.fileRepository = fileRepository;
        this.folderRepository = foldersRepository;
        this.userRepository = userRepository;
        this.streamer = streamer;
    }


    // папки /directory - 1C, 1R
    public CreateFolderResponseDto createFolder(String path, String username) {
        path = handleRequestAndReturnPath(path, username);
        if (!FolderUtil.isFolder(path)) {
            throw new ValidationException("This is no folder, this is file!");
        }
        if (folderRepository.isExist(path)) {
            throw new AlreadyExistException("Folder with this name already exist!");
        }

        if (!isExistParentalFolder(path)) {
            throw new NotFoundException("Parental folder not exist!");
        }

        folderRepository.save(path);
        ResourceResponseDto resourceDto = ResourceMapper.mapFolder(path);
        return new CreateFolderResponseDto(resourceDto.path(),
                resourceDto.name(),
                resourceDto.type());
    }


    public List<ResourceResponseDto> getFolder(String path, String username) {

        path = handleRequestAndReturnPath(path, username);

        if (!FolderUtil.isFolder(path)) {
            throw new ValidationException("This is no folder, this is file!");
        }

        if (folderRepository.isExist(path)) {

            return ResourceMapper.mapResourcesInFolder(folderRepository.getInfo(path));
        } else {
            log.info("not found {}", path);
            throw new NotFoundException("Folder not found!");
        }
    }


    // общее /resources - 1C, 3R, 1U, 1D

    public List<ResourceResponseDto> upload(String path, String username, List<MultipartFile> files) {
        path = handleRequestAndReturnPath(path, username);
        List<ResourceResponseDto> result = new ArrayList<>();


        if (MemoryCheckUtil.checkMemoryForMemoryOverflow(
                folderRepository.getSize(
                        handleRequestAndReturnPath("", username)) + files.size())) {
            long maxSize = MemoryCheckUtil.maxSizeFiles();
            throw new OutOfMemoryException(
                    "You're running low on disk space. Buy yourself a hard drive. Max size your files - %d MB"
                            .formatted(maxSize));
        }
        /**
         * Да, в ТЗ этого нет. Но мой сервер слабенький, туда нельзя загружать много файлов. Если загрузить, он упадёт.
         * Это мой сервер, за который плачу я из своего кармана. Если бы это была реальная рабочая задача, я бы сначала
         * объяснил это заказчику и попросил оплатить нормальный сервер, а при отказе сказал бы, что тогда без
         * ограничения всё упадёт, и попросил бы правки в ТЗ. В таком случае при падении сервера ответственность явно не
         * моя. А пока я плачу за сервер, у меня есть необходимость в таком отклонении от ТЗ.
         * */

        log.info("{} upload {}:", username, files.size());

        String filePath;
        StringBuilder filesNames = new StringBuilder();
        boolean exceptionFlag = false;
        StringBuilder failFilesDownloadNames = new StringBuilder();

        for (MultipartFile file : files) {
            if (file == null) {
                continue;
            }

            filePath = path + file.getOriginalFilename();

            if (fileRepository.isExist(filePath)) {
                exceptionFlag = true;
                failFilesDownloadNames.append(file.getOriginalFilename()).append("; ");
                continue;
            }

            try (InputStream stream = file.getInputStream()) {
                FileDto fileDto = fileRepository.save(
                        filePath,
                        stream,
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
        if (exceptionFlag) {
            throw new AlreadyExistException(
                    "This files names %s already exist. The download was interrupted. Was save files: %s"
                            .formatted(failFilesDownloadNames, filesNames));
        }

        return result;
    }


    public ResourceResponseDto getInfo(String path, String username) {

        path = handleRequestAndReturnPath(path, username);

        if (!FolderUtil.isFolder(path)) {
            return ResourceMapper.mapResource(
                    fileRepository.getInfo(path)
            );
        }
        if (folderRepository.isExist(path)) {

            return ResourceMapper.mapFolder(path);
        } else {
            throw new NotFoundException("Folder not found!");
        }
    }

    public void getResource(String path, OutputStream outputStream, String username) {
        path = handleRequestAndReturnPath(path, username);

        if (FolderUtil.isFolder(path)) {
            streamer.streamFolderAsZipFile(path, outputStream); // я принципиально не хотел бы в учебном проекте
        } else {                    // передавать byte[] вместо стрима для файлов - это плохая практика, мне кажется.
            streamer.streamFile(path, outputStream); // плюс у меня слабый сервер. Если бы это было реальное тз,
        }                           // я бы попросил скорректировать его. А если заказчик откажется, то попросил бы его
    }                               // самого оплатить нормальный сервер, объяснив, что этот упадёт. А пока за сервер
                                    // плачу я - приходится принимать такие решения поперёк тз, но в угоду решения
                                    // проблемы, которая последует за выбором byte[].

    public List<ResourceResponseDto> search(String query, String username) {

        var request = validateRequest(query, username);
        query = request.path;
        String userFolderPath = getUserPathByUsername(request.username, "");

        return ResourceMapper.mapResourcesInFolder(folderRepository.search(query, userFolderPath));

    }

    public ResourceResponseDto move(String from, String to, String username) {

        from = handleRequestAndReturnPath(from, username);
        to = handleRequestAndReturnPath(to, username);
        if (FolderUtil.isFolder(from) && FolderUtil.isFolder(to)) {
            if (folderRepository.isExist(to)) {
                throw new AlreadyExistException("Target folder already exist!");
            }
            folderRepository.move(from, to);
            return ResourceMapper.mapFolder(to);
        } else if (!FolderUtil.isFolder(from) && !FolderUtil.isFolder(to)) {
            if (fileRepository.isExist(to)) {
                throw new AlreadyExistException("Target file already exist!");
            }
            return ResourceMapper.mapResource(fileRepository.move(from, to));
        } else {
            throw new ValidationException("Path 1 and path 2 must be both folders or both files!");
        }

    }


    public void delete(String path, String username) {
        path = handleRequestAndReturnPath(path, username);

        if (FolderUtil.isFolder(path)) {
            if (!folderRepository.isExist(path)) {
                throw new NotFoundException("Folder not found!");
            }
            folderRepository.delete(path);
        } else {
            if (!fileRepository.isExist(path)) {
                throw new NotFoundException("File not found!");
            }
            fileRepository.delete(path);
        }

    }

    public void deleteAllUserFilesByUserId(Long id) {
        if (id == null) {
            throw new ValidationException("Not found userId");
        }

        String path = getUserPath(id, "");
        folderRepository.delete(path);
        log.info("Delete all files {}", path);
    }

    // Зачем эти два метода сверху и снизу, если их не требует ТЗ? Смотри пояснения в методе upload().

    public List<UsersSizeDto> getUsers() {
        List<User> users = userRepository.findAll();
        List<UsersSizeDto> result = new ArrayList<>();

        for (User user : users) {
            try {
                result.add(new UsersSizeDto(
                        user.getLogin(),
                        user.getId(),
                        folderRepository.getSize(getFolderName(user.getId()))
                ));
            } catch (RuntimeException e) {
                log.info("Error get user! {}", user.getLogin());
                continue;
            }
        }
        return result;
    }


    private boolean isExistParentalFolder(String path) {
        String[] folders = path.split("/");
        String fileName = folders[folders.length - 1];
        String folderParentName = path.substring(0, path.length() - (fileName.length() + 1));


        return folderRepository.isExist(folderParentName);
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

        if (!folderRepository.isExist(folderName)) {
            folderRepository.save(folderName);
        }

    }

    private String handleRequestAndReturnPath(String path, String username) {
        ValidatedRequestDto validated = validateRequest(path, username);

        return getUserPathByUsername(validated.username, validated.path);

    }

    private @NonNull String getUserPathByUsername(String username, String path) {
        Long userId = getUserIdAndCreatePath(username);
        return getUserPath(userId, path);
    }

    private Long getUserIdAndCreatePath(String username) {
        Long userId = userRepository.findUsersByLogin(username).orElseThrow(() -> new NotFoundException("User not found!")).getId();
        createUserFolder(userId);
        return userId;
    }

    private static @NonNull ValidatedRequestDto validateRequest(String path, String username) {
        username = Validator.validateUsername(username);
        path = Validator.validatePath(path);
        return new ValidatedRequestDto(path, username);
    }

    private record ValidatedRequestDto(String path, String username) {
    }

}
