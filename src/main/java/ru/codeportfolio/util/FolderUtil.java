package ru.codeportfolio.util;

public final class FolderUtil {
    private FolderUtil() {
    }

    public static boolean isFolder(String path) {
        return path.charAt(path.length() - 1) == '/';
    }

}
