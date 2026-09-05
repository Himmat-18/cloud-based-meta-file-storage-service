package org.example.cloud_storage.dto;

public class LoginResponse {

    private final Long id;
    private final String name;
    private final String email;
    private final String role;
    private final String token;

    public LoginResponse(Long id,
                         String name,
                         String email,
                         String role,
                         String token) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}