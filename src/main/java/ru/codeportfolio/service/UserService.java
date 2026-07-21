package ru.codeportfolio.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.codeportfolio.dao.FilesRepository;
import ru.codeportfolio.dao.UserRepository;
import ru.codeportfolio.dto.UserDto;
import ru.codeportfolio.exception.AlreadyExistException;
import ru.codeportfolio.exception.NotFoundException;
import ru.codeportfolio.model.Role;
import ru.codeportfolio.model.User;

@Transactional
@Service
public class UserService {

    private final UserRepository userRepository;
    private final FilesRepository filesRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, FilesRepository filesRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.filesRepository = filesRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public UserDto createUser(String username, String password) {
        // todo validation

        password = passwordEncoder.encode(password);
        User user;
        try {
            user = userRepository.save(new User(username, password, Role.USER));
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistException("Username %s already exist.".formatted(username));
        }


        return new UserDto(user.getLogin());
    }

    public UserDto getInfo(String username) {
        User user = userRepository.findUsersByLogin(username).orElseThrow(
                () -> new NotFoundException("user %s not found".formatted(username)));

        return new UserDto(user.getLogin());
    }

}
