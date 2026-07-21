package ru.codeportfolio.models;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "users")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 45)
    private String login;


    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Role role;

    public User(String login, String password, Role role) {
        this.login = login;
        this.password = password;
        this.role = role;
    }

    public User() {

    }
}
