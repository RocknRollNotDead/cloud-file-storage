package ru.codeportfolio.dto;

import ru.codeportfolio.model.TypeFile;

public record ResourceResponseDto(
        String path,
        String name,
        Long size,
        TypeFile type
) {
}
