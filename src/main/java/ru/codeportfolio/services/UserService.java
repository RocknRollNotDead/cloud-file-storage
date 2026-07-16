package ru.codeportfolio.services;

import org.springframework.data.domain.Example;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.codeportfolio.dao.UserRepository;
import ru.codeportfolio.dto.UserDto;
import ru.codeportfolio.exceptions.AlreadyExistException;
import ru.codeportfolio.exceptions.NotFoundException;
import ru.codeportfolio.models.Role;
import ru.codeportfolio.models.User;

@Transactional
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public UserDto createUser(String username, String password) {
        password = passwordEncoder.encode(password);

        userRepository.save(new User(username, password, Role.USER));
        return new UserDto(
                userRepository
                        .findUsersByLogin(username)
                        .orElseThrow(AlreadyExistException::new)
                        .getLogin());
    }

    @Transactional(readOnly = true)
    public UserDto logIn(String username, String password) {
        password = passwordEncoder.encode(password);

        if (userRepository.exists(
                Example.of(new User(username, password, Role.USER))
        )){
            return new UserDto(userRepository
                    .findUsersByLogin(username)
                    .orElseThrow(() -> new NotFoundException(username + " non found"))
                    .getLogin());
        }
        return null;
    }


    public UserDto getInfo(String username) {
        User user = userRepository.findUsersByLogin(username).orElseThrow(
                () -> new NotFoundException("user " + username + " not found"));

        return new UserDto(user.getLogin());
    }

    public void logOut() {

    }
}
