package ru.codeportfolio.models;

import jakarta.persistence.*;

@Entity
@Table(name = "users")

public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(unique = true, nullable = false)
    private String password;

    public Users(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public Users() {

    }

    public String getPassword() {
        return password;
    }

    public String getLogin() {
        return login;
    }
}
