package ru.codeportfolio.services;

import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.codeportfolio.dao.UserRepository;
import ru.codeportfolio.dto.UserDto;
import ru.codeportfolio.models.Users;

@Service
public class UsersService {

    private final UserRepository userRepository;

    public UsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDto createUser(String username, String password) {
        userRepository.save(new Users(username, password));
        return new UserDto(
                userRepository
                        .findUsersByLogin(username)
                        .getFirst()
                        .getLogin());
    }

    public UserDto logIn(String username, String password) {
        if (userRepository.exists(
                Example.of(new Users(username, password))
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
