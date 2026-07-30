package ru.codeportfolio.util;

import ru.codeportfolio.exception.ValidationException;

public final class Validator {
    private final static int minLengthPassword = 5;

    private Validator() {
    }

    public static String validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Error to validation username. Your username = \"%s\"".formatted(username));
        }
        return username.trim();
    }

    public static String validatePasswordWithLength(String password) {
        password = validatePassword(password);

        if (password.length() < minLengthPassword) {
            throw new ValidationException("Password length must be more than %d symbols".formatted(minLengthPassword));
        }
        return password;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("Error to validation password. Your password = \"%s\"".formatted(password));
        }
        return password.trim();
    }

    public static String validatePath(String path) {
        if (path == null) {
            throw new ValidationException("Error to validation path. Your path = \"%s\"".formatted(path));
        }
        if (!path.isBlank() && path.charAt(0) == '/') {
            path = path.substring(1);
        }
        if (path.contains("//")) {
            throw new ValidationException("Invalid path. Path contain \"//\"");
        }
        // если в тз добавится запрет определенных символов, то я буду валидировать их именно здесь.
        return path.trim();
    }
}
