package ru.codeportfolio.controllers;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

        ResourceResponseDto responseDto = service.getInfo(path);
        return ResponseEntity.ok(responseDto);
    }


    @DeleteMapping("/")
    public ResponseEntity delete(
            @RequestParam String path) {

        service.delete(path);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(
            @RequestParam String path) {

        Resource resource = service.getResource(path);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    @PostMapping("/move")
    public ResponseEntity<ResourceResponseDto> move(
            @RequestParam String from,
            @RequestParam String to) {

        ResourceResponseDto responseDto = service.move(from, to);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResourceResponseDto>> search(
            @RequestParam String query) {

        List<ResourceResponseDto> responseDto = service.search(query);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/")
    public ResponseEntity<List<ResourceResponseDto>> upload(
            @RequestParam String path) {// плюс ресурсы

        List<ResourceResponseDto> responseDto = service.upload(path);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

    }








}
