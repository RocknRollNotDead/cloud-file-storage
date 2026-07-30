package ru.codeportfolio.util;

public final class MemoryCheckUtil {
    public static final long MAX_SIZE_STORAGE_FOR_ONE_USER = 500_000_000L;
    public static final long MEGABYTE = 1_000_000L;

    private MemoryCheckUtil() {
    }

    public static long maxSizeFiles() {
        return Math.ceilDiv(MAX_SIZE_STORAGE_FOR_ONE_USER, MEGABYTE);
    }

    public static boolean checkMemoryForMemoryOverflow(Long size) {
        return size > MAX_SIZE_STORAGE_FOR_ONE_USER;
    }
}
