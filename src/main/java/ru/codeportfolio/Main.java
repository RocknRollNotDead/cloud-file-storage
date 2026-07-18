package ru.codeportfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    // план (после подтягивания зависимостей и настройки докера✅)
    // сначала написать контроллеры с эндпоинтами, создать дто по необходимости✅
    // (2 контроллера - авторизация и работа с файлами✅
    // (загрузка, выгрузка, инфа✅))
    // сделать сервисы ..
    // сделать дао которое обращается к S3   и redis✅ и (sql бд - mariaDB✅)
    // интегрировать сессии ...

    public static void main(String[] args) {
        System.out.println("Hello world!");
        SpringApplication.run(Main.class, args);
    }

}
