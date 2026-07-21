package ru.codeportfolio.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.codeportfolio.dto.ResourceResponseDto;
import ru.codeportfolio.exceptions.DataAccessException;
import ru.codeportfolio.exceptions.ValidationException;
import ru.codeportfolio.services.FilesService;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/resource")
@Tag(name = "Ресурсы", description = "Операции с файлами и папками")
public class FilesController {

    private final FilesService service;

    public FilesController(FilesService service) {
        this.service = service;
    }


    @Operation(summary = "Получить информацию о файле или папке")
    @ApiResponse(responseCode = "200", description = "Ресурс найден")
    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь к ресурсу")
    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    @ApiResponse(responseCode = "404", description = "Ресурс не найден")
    @GetMapping()
    public ResponseEntity<ResourceResponseDto> getInfo(
            @RequestParam String path,
            @AuthenticationPrincipal UserDetails principal) {

        ResourceResponseDto responseDto = service.getInfo(path, principal.getUsername());
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Удалить ресурс")
    @ApiResponse(responseCode = "204", description = "Успешно удалено")
    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь к ресурсу")
    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    @ApiResponse(responseCode = "404", description = "Ресурс не найден")

    @DeleteMapping()
    public ResponseEntity<Void> delete(
            @RequestParam String path,
            @AuthenticationPrincipal UserDetails principal) {

        log.info(path);
        service.delete(path, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    /*@GetMapping("/download")
    public ResponseEntity<Resource> download(
            @RequestParam String path) {

        Resource resource = service.getResource(path);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }*/


    @Operation(summary = "Скачать ресурс")
    @ApiResponse(responseCode = "200", description = "Успешно начато скачивание")
    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь к ресурсу")
    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    @ApiResponse(responseCode = "404", description = "Ресурс не найден")
    @GetMapping("/download")
    public /*ResponseEntity<byte[]>*/ void downloadFile(@RequestParam String path,
                                                        HttpServletResponse response,
                                                        @AuthenticationPrincipal UserDetails principal) throws IOException {
     /*   byte[] result = service.getResource(path);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(result);
*/
        log.info(path);
        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archive.zip\"");

        service.getResource(path, response.getOutputStream(), principal.getUsername());

    }


    @Operation(summary = "Переместить ресурс")
    @ApiResponse(responseCode = "200", description = "Успешно перемещено")
    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь к ресурсу")
    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    @ApiResponse(responseCode = "404", description = "Ресурс не найден")
    @ApiResponse(responseCode = "409", description = "Ресурс по целевому пути уже существует")
    @PostMapping("/move")
    public ResponseEntity<ResourceResponseDto> move(
            @RequestParam String from,
            @RequestParam String to,
            @AuthenticationPrincipal UserDetails principal) {

        log.info(from + "  " + to);
        ResourceResponseDto responseDto = service.move(from, to, principal.getUsername());
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Найти ресурс")
    @ApiResponse(responseCode = "200", description = "Успешный запрос")
    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь к ресурсу")
    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    @GetMapping("/search")
    public ResponseEntity<List<ResourceResponseDto>> search(
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails principal) {

        List<ResourceResponseDto> responseDto = service.search(query, principal.getUsername());
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Загрузить(создать) ресурс")
    @ApiResponse(responseCode = "201", description = "Успешно создано")
    @ApiResponse(responseCode = "400", description = "Невалидное тело запроса")
    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    @ApiResponse(responseCode = "409", description = "Файл по целевому пути уже существует")
    @PostMapping()
    public ResponseEntity<List<ResourceResponseDto>> upload(
            @RequestParam String path,
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam List<MultipartFile> object) {

        if (object == null){
            throw new ValidationException("Non file found!");
        }

        List<ResourceResponseDto> responseDto = service.upload(path,
                principal.getUsername(), object);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

    }








}
