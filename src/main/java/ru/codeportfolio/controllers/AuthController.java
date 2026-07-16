package ru.codeportfolio.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.codeportfolio.dto.UserDto;
import ru.codeportfolio.services.UsersService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsersService service;

    public AuthController(UsersService service) {
        this.service = service;
    }

    @PostMapping("/sing-in")
    public ResponseEntity<UserDto> logIn(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "password", required = false) String password) {

        UserDto userDto = service.logIn(username, password);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/sing-up")
    public ResponseEntity<UserDto> createUser(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "password", required = false) String password) {

        UserDto userDto = service.createUser(username, password);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PostMapping("/sing-out")
    public ResponseEntity logOut() {
        service.logOut();
        return ResponseEntity.noContent().build();
    }



}