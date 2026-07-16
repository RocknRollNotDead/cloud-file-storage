package ru.codeportfolio.dto;

import ru.codeportfolio.models.TypeFile;

public record CreateFolderResponseDto(
        String path,
        String name,
        TypeFile type
) {
}
