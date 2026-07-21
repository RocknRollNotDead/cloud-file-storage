package ru.codeportfolio.dto.db;

import ru.codeportfolio.model.TypeFile;

public record FileDto(String name,
                      Long size,
                      TypeFile typeFile) {

}
