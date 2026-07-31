package ru.codeportfolio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

@Configuration
@EnableRedisHttpSession
public class SessionConfig {
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        JsonMapper mapper = JsonMapper.builder()
                .addModules(SecurityJacksonModules.getModules(getClass().getClassLoader()))
                .build();

        return new GenericJacksonJsonRedisSerializer(mapper);
    }

    @Bean
    public SessionRepositoryCustomizer<RedisSessionRepository> sessionTimeoutCustomizer(
            @Value("${spring.session.timeout}") Duration timeout) {
        return repository -> repository.setDefaultMaxInactiveInterval(timeout);
    }

}
