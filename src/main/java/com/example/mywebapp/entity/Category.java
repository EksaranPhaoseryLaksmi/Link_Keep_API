package com.example.mywebapp.entity;
import lombok.Data;
@Data
public class Category {
    private Long id;
    private String name;
    private String describtion;
    private String created_by;
    private String created_date;
}
