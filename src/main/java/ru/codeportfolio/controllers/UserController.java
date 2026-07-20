package ru.codeportfolio.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.codeportfolio.dto.UserDto;
import ru.codeportfolio.services.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @Operation(summary = "Получить информация о юзере")
    @ApiResponse(responseCode = "200", description = "Успешно получена информация")
    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getInfo(@AuthenticationPrincipal UserDetails principal) { // тот обьект который в SecurityConfig

        UserDto userDto = service.getInfo(principal.getUsername());
        return ResponseEntity.ok(userDto);
    }

}
