package ru.codeportfolio.controller;

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

    @PostMapping("/sign-in")
    public ResponseEntity<UserDto> logIn(HttpServletRequest httpRequest,
                                         HttpServletResponse response,
                                         @RequestBody(required = false) RequestAuthDto req) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.username(),
                        req.password()
                )
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

//        httpRequest.getSession(true)
//                .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context); todo разобраться
        SecurityContextRepository repository = new HttpSessionSecurityContextRepository();
        repository.saveContext(context, httpRequest, response);

        // если необходимо будет 100% возвращения тела запроса UserDto, то
//        response.setContentType("application/json");
//        response.getWriter().write(
//                objectMapper.writeValueAsString(new UserDto(req.username()))
//        );

        return ResponseEntity.ok(new UserDto(req.username()));


    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserDto> createUser(
            @RequestBody(required = false) RequestAuthDto req) {

        UserDto userDto = service.createUser(req.username(), req.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);

        // сделать автоматический лог ин
    }

/*    @PostMapping("/sign-out")
    public ResponseEntity logOut() {
        service.logOut();
        return ResponseEntity.noContent().build();
    }*/


}