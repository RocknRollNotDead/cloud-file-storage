package ru.codeportfolio.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.codeportfolio.dao.UserRepository;
import ru.codeportfolio.model.Role;
import ru.codeportfolio.model.User;

@Component
public class UsersDbInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UsersDbInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String login = System.getenv("DB_LOGIN");
        if (userRepository.findUsersByLogin(login).isEmpty()) {
            userRepository.save(new User(
                    login,
                    passwordEncoder.encode(System.getenv("DB_PASSWORD")),
                    Role.ADMIN
                    )
            );
        }
        login = "user1";
        if (userRepository.findUsersByLogin(login).isEmpty()) {
            userRepository.save(new User(
                            login,
                            passwordEncoder.encode(System.getenv("PASSWORD_USER")),
                            Role.USER
                    )
            );
        }


    }


}
