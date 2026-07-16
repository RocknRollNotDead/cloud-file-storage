package ru.codeportfolio.controllers;

import jdk.jfr.ContentType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.codeportfolio.dto.ResourceResponseDto;
import ru.codeportfolio.services.FilesService;

import java.util.List;

@RestController
@RequestMapping("/resource")
public class FilesController {

    private final FilesService service;

    public FilesController(FilesService service) {
        this.service = service;
    }


    @GetMapping("/")
    public ResponseEntity<ResourceResponseDto> getInfo(
            @RequestParam String path) {

        ResourceResponseDto responceDto = service.getInfo(path);
        return ResponseEntity.ok(responceDto);
    }


    @DeleteMapping("/")
    public ResponseEntity delete(
            @RequestParam String path) {

        service.delete(path);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download")
    @ContentType("application/octet-stream")
    public ResponseEntity<ResourceResponseDto> download(
            @RequestParam String path) {

        Resource resource = service.getResource(path); // как отправить файл?
        return ResponseEntity.ok(resource); // setContentType
    }

    @PostMapping("/move")
    public ResponseEntity<ResourceResponseDto> move(
            @RequestParam String from,
            @RequestParam String to) {

        ResourceResponseDto responceDto = service.move(from, to);
        return ResponseEntity.ok(responceDto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResourceResponseDto>> search(
            @RequestParam String query) {

        List<ResourceResponseDto> responceDto = service.search(query);
        return ResponseEntity.ok(responceDto);
    }

    @PostMapping("/")
    public ResponseEntity<List<ResourceResponseDto>> upload(
            @RequestParam String path) {// плюс ресурсы

        List<ResourceResponseDto> responceDto = service.upload(path);
        return ResponseEntity.status(HttpStatus.CREATED).body(responceDto);

    }








}
