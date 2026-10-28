package com.ipcms.dto;

import com.ipcms.model.Role;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private Role role;
    private String fullName;
    private int sessionDurationHours;

    public AuthResponse() {}

    public AuthResponse(String token, Long userId, String username, String email, Role role, String fullName, int sessionDurationHours) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
        this.sessionDurationHours = sessionDurationHours;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getSessionDurationHours() { return sessionDurationHours; }
    public void setSessionDurationHours(int sessionDurationHours) { this.sessionDurationHours = sessionDurationHours; }
}
