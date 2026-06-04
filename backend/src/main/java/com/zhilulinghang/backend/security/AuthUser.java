package com.zhilulinghang.backend.security;

public class AuthUser {
    private final Long id;
    private final String username;
    private final String role;

    public AuthUser(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
