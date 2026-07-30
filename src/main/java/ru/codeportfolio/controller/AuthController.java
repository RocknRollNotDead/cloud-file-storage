package ru.codeportfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.codeportfolio.dto.RequestAuthDto;
import ru.codeportfolio.dto.UserDto;
import ru.codeportfolio.exception.ValidationException;
import ru.codeportfolio.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService service;

    public AuthController(AuthenticationManager authenticationManager, UserService service) {
        this.authenticationManager = authenticationManager;
        this.service = service;
    }

    @Operation(summary = "Авторизоваться")
    @ApiResponse(responseCode = "200", description = "Успех")
    @ApiResponse(responseCode = "401", description = "Неправильные пароль или username (отсутствуют совпадения в бд)")
    @PostMapping("/sign-in")
    public ResponseEntity<UserDto> logIn(HttpServletRequest httpRequest,
                                         HttpServletResponse response,
                                         @RequestBody(required = false) RequestAuthDto req) {

        if (req == null || req.username() == null || req.password() == null) {
            throw new ValidationException("Invalid request! Body of request or username or password is empty!");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.username(),
                        req.password()
                )
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        SecurityContextRepository repository = new HttpSessionSecurityContextRepository();
        repository.saveContext(context, httpRequest, response);

        return ResponseEntity.ok(new UserDto(req.username()));


    }

    @Operation(summary = "Зарегистрироваться")
    @ApiResponse(responseCode = "201", description = "Успех")
    @ApiResponse(responseCode = "400", description = "Невалидный аргумент (пароль или username)")
    @ApiResponse(responseCode = "409", description = "username занят другим пользователем")
    @PostMapping("/sign-up")
    public ResponseEntity<UserDto> createUser(
            @RequestBody(required = false) RequestAuthDto req) {

        UserDto userDto = service.createUser(req.username(), req.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }
}