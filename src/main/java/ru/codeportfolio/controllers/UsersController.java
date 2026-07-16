package ru.codeportfolio.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.codeportfolio.dto.UserDto;
import ru.codeportfolio.services.UserService;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UserService service;

    public UsersController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getInfo(Authentication authentication) {
        String username = authentication.name();
        UserDto userDto = service.getInfo(username);
        return ResponseEntity.ok(userDto);
    }

/*
    @GetMapping()
    public ResponseEntity<MatchesResponseDto> getMatches(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "player_name", required = false) String playerName) {
        MatchesResponseDto matches = service.getAllMatches(page, playerName);
        return ResponseEntity.ok(matches);

    }*/



}
