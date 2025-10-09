package com.example.mywebapp.entity;
import lombok.Data;

@Data
public class CategoryRequest {
    private String url;
    private String name;
    private String description;
    private String created_by;
    private String sub;
}
