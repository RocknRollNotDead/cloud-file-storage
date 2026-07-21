package ru.codeportfolio.dto;

import ru.codeportfolio.model.TypeFile;

public record CreateFolderResponseDto(
        String path,
        String name,
        TypeFile type
) {
}
