package com.example.mywebapp.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String password;
    private String team_id;
    private String team_name;
    private String email_verified_at;
    private String remember_token;
    private LocalDateTime reset_token_created_at;
}
