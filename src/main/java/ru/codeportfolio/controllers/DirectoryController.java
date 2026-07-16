package ru.codeportfolio.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.codeportfolio.dto.CreateFolderResponseDto;
import ru.codeportfolio.dto.ResourceResponseDto;
import ru.codeportfolio.services.FilesService;

import java.util.List;

@RestController
@RequestMapping("/directory")
public class DirectoryController {
    private final FilesService service;

    public DirectoryController(FilesService service) {
        this.service = service;
    }

    @GetMapping("/")
    public ResponseEntity<List<ResourceResponseDto>> getFolder(
            @RequestParam String path) {

        List<ResourceResponseDto> responseDto = service.getFolder(path);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/")
    public ResponseEntity<CreateFolderResponseDto> createFolder(
            @RequestParam String path) {

        CreateFolderResponseDto responseDto = service.createFolder(path);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
