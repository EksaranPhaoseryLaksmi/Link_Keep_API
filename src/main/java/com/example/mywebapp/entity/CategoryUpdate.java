package com.example.mywebapp.entity;
import lombok.Data;

@Data
public class CategoryUpdate {
    private int id;
    private String url;
    private String name;
    private String description;
    private String updated_by;
    private String sub;
    private String is_active;
}
