package com.onepagebuilder.backend.security;

/**
 * Simple class to hold authenticated user information from JWT token
 */
public class JwtUser {
    private final Long id;
    private final String email;
    private final String role;

    public JwtUser(Long id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
