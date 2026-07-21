package ru.codeportfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.codeportfolio.dto.CreateFolderResponseDto;
import ru.codeportfolio.dto.ResourceResponseDto;
import ru.codeportfolio.service.FilesService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directory")
public class DirectoryController {
    private final FilesService service;

    public DirectoryController(FilesService service) {
        this.service = service;
    }

    @Operation(summary = "Получить информация о папке")
    @ApiResponse(responseCode = "200", description = "Успешно получена информация")
    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь к папке")
    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    @ApiResponse(responseCode = "404", description = "Папка не найдена")
    @GetMapping()
    public ResponseEntity<List<ResourceResponseDto>> getFolder(
            @RequestParam String path,
            @AuthenticationPrincipal UserDetails principal){

        List<ResourceResponseDto> responseDto = service.getFolder(path, principal.getUsername());
        return ResponseEntity.ok(responseDto);

        // content type
    }


    @Operation(summary = "Создать папку")
    @ApiResponse(responseCode = "201", description = "Успех")
    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь к папке")
    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    @ApiResponse(responseCode = "404", description = "Нет родительской папки")
    @ApiResponse(responseCode = "409", description = "такая папка уже есть")
    @PostMapping()
    public ResponseEntity<CreateFolderResponseDto> createFolder(
            @RequestParam String path,
            @AuthenticationPrincipal UserDetails principal) {
        String username = principal.getUsername();
        log.debug(username);
        CreateFolderResponseDto responseDto = service.createFolder(path, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
