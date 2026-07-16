package ru.codeportfolio.dto;

import ru.codeportfolio.models.TypeFile;

public record ResourceResponseDto(
        String path,
        String name,
        Integer size,
        TypeFile type
) {
}
