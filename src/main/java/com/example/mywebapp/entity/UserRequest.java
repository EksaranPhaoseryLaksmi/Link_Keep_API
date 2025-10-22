package com.example.mywebapp.entity;

import lombok.Data;

@Data
public class UserRequest {
    private String name;
    private String email;
    private String username;
    private String password;
    private String team_id;
    private Boolean verified;
    private String remember_token;
    private String email_verified_at;
}
