package ru.codeportfolio.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import ru.codeportfolio.dto.UserDto;
import ru.codeportfolio.models.Role;
import ru.codeportfolio.services.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService service;

    public AuthController(AuthenticationManager authenticationManager, UserService service) {
        this.authenticationManager = authenticationManager;
        this.service = service;
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UserDto> logIn( HttpServletRequest httpRequest,
            @RequestBody(required = false) String username,
            @RequestBody(required = false) String password) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        httpRequest.getSession(true)
                .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

//        UserDto userDto = service.logIn(username, password);
        return ResponseEntity.ok(new UserDto(username, Role.USER));


    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserDto> createUser(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "password", required = false) String password) {

        UserDto userDto = service.createUser(username, password);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);

        // сделать автоматический лог ин
    }

/*    @PostMapping("/sign-out")
    public ResponseEntity logOut() {
        service.logOut();
        return ResponseEntity.noContent().build();
    }*/



}