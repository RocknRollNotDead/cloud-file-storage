package ru.codeportfolio.controllers.other;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.codeportfolio.dao.UserRepository;
import ru.codeportfolio.models.User;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/", "/index.html", "/config.js", "/assets/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
//                .formLogin(form -> form
//                        .loginProcessingUrl("/auth/sign-in")
//                        .successHandler((request, response, authentication) -> response.setStatus(200))
//                        .failureHandler((request, response, exception) -> response.setStatus(401))
//                        .permitAll()
//                )
//                .formLogin(form -> form
//                        .loginPage("/auth/sign-in")
//                        .permitAll()
//                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/sign-out")
//                        .logoutSuccessUrl("/registration")
                        .permitAll()
                );
        // todo настроить обработку исключений

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(){
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                // именно userRepository, а не service, потому что мне нужен user вместе с password,
                // а service password не даёт
                User user =  userRepository
                        .findUsersByLogin(username)
                        .orElseThrow(() -> new UsernameNotFoundException("username not exist " + username));
                return org.springframework.security.core.userdetails.User
                        .withUsername(username)
                        .password(user.getPassword())
                        .roles(user.getRole().toString())
                        .build();
            }
        };
    }
}