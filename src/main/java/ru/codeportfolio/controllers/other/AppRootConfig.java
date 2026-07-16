package ru.codeportfolio.controllers.other;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.codeportfolio.Config;
import ru.codeportfolio.models.User;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan(basePackages = { "ru.codeportfolio.services", "ru.codeportfolio.dao" })
public class AppRootConfig {

    private static final String NAME_SQL = "mariadb";

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:" + NAME_SQL + "://" + Config.URL);
        config.setUsername(Config.getLogin()); // "postgres" - default
        config.setPassword(Config.getPassword());
        config.setDriverClassName("org.postgresql.Driver");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(30000);

        return new HikariDataSource(config);
    }

    @Bean
    public SessionFactory sessionFactory(DataSource dataSource) {
        Map<String, Object> settings = new HashMap<>();

        settings.put("hibernate.connection.datasource", dataSource);
        settings.put("hibernate.show_sql", true);
        settings.put("hibernate.hbm2ddl.auto", "update");

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySettings(settings)
                .build();

        MetadataSources sources = new MetadataSources(registry);
        sources.addAnnotatedClass(User.class);

        Metadata metadata = sources.getMetadataBuilder().build();
        return metadata.getSessionFactoryBuilder().build();
    }




}
