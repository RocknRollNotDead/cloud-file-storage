package ru.codeportfolio.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.codeportfolio.dto.UserDto;
import ru.codeportfolio.services.UsersService;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UsersService service;

    public UsersController(UsersService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getInfo(/* тут будет jwt*/) {

        UserDto userDto = service.getInfo(/*jwt*/);
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
