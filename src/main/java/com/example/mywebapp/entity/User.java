package com.example.mywebapp.entity;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String name;
    private String username;
    private String password;
    private String team_id;
    private String team_name;
}
