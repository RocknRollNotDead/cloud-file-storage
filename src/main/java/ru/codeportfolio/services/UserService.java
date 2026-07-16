package ru.codeportfolio.services;

import org.springframework.data.domain.Example;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.codeportfolio.dao.UserRepository;
import ru.codeportfolio.dto.UserDto;
import ru.codeportfolio.models.User;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDto createUser(String username, String password) {
        password = passwordEncoder.encode(password);

        userRepository.save(new User(username, password));
        return new UserDto(
                userRepository
                        .findUsersByLogin(username)
                        .getFirst()
                        .getLogin());
    }

    public UserDto logIn(String username, String password) {
        password = passwordEncoder.encode(password);

        if (userRepository.exists(
                Example.of(new User(username, password))
        )){
            return new UserDto(userRepository
                    .findUsersByLogin(username)
                    .getFirst()
                    .getLogin());
        }
        return null;
    }

    public UserDto getInfo() {


        return null;
    }

    public void logOut() {

    }
}
