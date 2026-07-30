package ru.codeportfolio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.codeportfolio.dto.UsersSizeDto;
import ru.codeportfolio.service.FileService;

import java.util.List;

@RestController
@RequestMapping("/admin-panel")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final FileService fileService;

    public AdminController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UsersSizeDto>> getUsers() {
        return ResponseEntity.ok(fileService.getUsers());

    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUserFiles(@PathVariable Long id) {
        fileService.deleteAllUserFilesByUserId(id);
        return ResponseEntity.noContent().build();
    }
}
