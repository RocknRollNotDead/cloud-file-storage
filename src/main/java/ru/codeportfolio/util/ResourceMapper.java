package ru.codeportfolio.util;

import org.jspecify.annotations.NonNull;
import ru.codeportfolio.dto.ResourceResponseDto;
import ru.codeportfolio.dto.db.FileDto;
import ru.codeportfolio.model.TypeFile;

import java.util.ArrayList;
import java.util.List;

public final class ResourceMapper {
    private ResourceMapper() {
    }

    public static ResourceResponseDto mapResource(FileDto fileDto) {
        String[] pathAndName = fileDto.name().split("/");

        String name = pathAndName[pathAndName.length - 1];
        String path = getPathFromPathAndName(pathAndName, true);

        return new ResourceResponseDto(path, name, fileDto.size(), TypeFile.FILE);
    }

    public static ResourceResponseDto mapFolder(String path) {
        String[] pathAndName = path.split("/");
        String nameFolder = pathAndName[pathAndName.length - 1];

        path = getPathFromPathAndName(pathAndName, true);

        return new ResourceResponseDto(
                path,
                nameFolder,
                null,
                TypeFile.DIRECTORY
        );
    }

    private static @NonNull String getPathFromPathAndName(String[] pathAndName, boolean hasUsernameInPath) {
        String path;
        StringBuilder stringBuilder = new StringBuilder();
        int firstIndex;
        stringBuilder.append("/");

        firstIndex = hasUsernameInPath ? 1 : 0;

        for (int i = firstIndex; i < pathAndName.length - 1; i++) {
            stringBuilder.append(pathAndName[i]);
            stringBuilder.append("/");
        }

        path = stringBuilder.toString();
        return path;
    }

    public static List<ResourceResponseDto> mapResourcesInFolder(List<FileDto> files) {
        List<ResourceResponseDto> result = new ArrayList<>();

        for (FileDto fileDto : files) {
            String path = fileDto.name();
            String[] pathAndName = path.split("/");
            String nameFolder = pathAndName[pathAndName.length - 1];
            path = getPathFromPathAndName(pathAndName, true);

            result.add(
                    new ResourceResponseDto(
                            path,
                            nameFolder,
                            fileDto.size(),
                            fileDto.typeFile()
                    )
            );

        }
        return result;
    }
}
