package ru.codeportfolio.services;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.codeportfolio.dto.CreateFolderResponseDto;
import ru.codeportfolio.dto.ResourceResponseDto;

import java.util.List;

@Service
public class FilesService {
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

    public Resource getResource(String path) {
        // прочитать сессию, узнать юзера
        // по юзеру достать файл
        return null;
    }
}
