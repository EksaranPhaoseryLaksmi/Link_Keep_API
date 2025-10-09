package com.example.mywebapp.entity;

import lombok.Data;

@Data
public class UserRequest {
    private String name;
    private String username;
    private String password;
    private String team_id;
}
