package ru.codeportfolio.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.codeportfolio.dto.UserDto;
import ru.codeportfolio.services.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getInfo(@AuthenticationPrincipal UserDetails principal) { // тот обьект который в SecurityConfig

        UserDto userDto = service.getInfo(principal.getUsername());
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
