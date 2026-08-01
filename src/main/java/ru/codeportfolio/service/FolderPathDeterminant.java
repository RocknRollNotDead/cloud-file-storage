package ru.codeportfolio.service;

import org.springframework.stereotype.Component;

@Component
public class FolderPathDeterminant {

    public static final String USER_FOLDER_NAME = "user-%d-files";

    public FolderPathDeterminant() {
    }

    public String getParentalFolderName(String path){
        String[] folders = path.split("/");
        String fileName = folders[folders.length - 1];
        return path.substring(0, path.length() - (fileName.length() + 1));
    }

    public String getFolderName(Long userId) {
        return USER_FOLDER_NAME.formatted(userId);
    }

    public String getPathWithUserFolder(Long userId, String path) {
        return "%s/%s".formatted(
                getFolderName(userId),
                path
        );
    }

}
