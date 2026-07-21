package ru.codeportfolio.util;

import ru.codeportfolio.exceptions.ValidationException;

public final class Validator {
    private Validator() {
    }

    public static String validateUsername(String username){
        if(username == null || username.isBlank()){
            throw new ValidationException("Error to validation username. Your username = \"%s\"".formatted(username));
        }
        return username.trim();
    }

    public static String validatePath(String path){
        if(path == null || path.isBlank()){
            throw new ValidationException("Error to validation path. Your path = \"%s\"".formatted(path));
        }
        return path.trim();
    }
}
