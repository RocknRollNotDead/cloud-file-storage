package ru.codeportfolio.model;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    // implements
    USER,
    ADMIN;

    @Override
    public @NonNull String getAuthority() {
        return "ROLE_" + name();
    }
}
