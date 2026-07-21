package ru.codeportfolio.dto.db;

import ru.codeportfolio.models.TypeFile;

public record FileDto(String name,
                      Long size,
                      TypeFile typeFile) {

}
