package ru.codeportfolio;

public class Config {
    public static final String URL = System.getenv("DB_URL");

    public static String getPassword(){
        return System.getenv("DB_PASSWORD");
    }
    public static String getLogin(){
        return System.getenv("DB_LOGIN");
    }


}
