package com.example.mywebapp.entity;
import lombok.Data;

@Data
public class LinkRequest {
    private String url;
    private String title;
    private String description;
    private String created_by;
    private String category_id;
    private String Status;
}
