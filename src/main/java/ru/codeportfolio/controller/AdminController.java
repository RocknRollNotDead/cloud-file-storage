package ru.codeportfolio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.codeportfolio.dto.UsersSizeDto;
import ru.codeportfolio.service.FilesService;

import java.util.List;

@RestController
@RequestMapping("/admin-panel")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final FilesService filesService;

    public AdminController(FilesService filesService) {
        this.filesService = filesService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UsersSizeDto>> getUsers() {
        return ResponseEntity.ok(filesService.getUsers());

    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUserFiles(@PathVariable Long id) {
        filesService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
