package ru.codeportfolio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Облачное хранилище файлов")
                        .description("""
                        Здесь вы можете хранить небольшие файлы, у каждого человека лимит 1 гб.
                        После переполнения памяти моего хостинга я удалю все файлы того пользователя,
                        общий размер файлов котрого будет больше всех.
    """));
    }
}
