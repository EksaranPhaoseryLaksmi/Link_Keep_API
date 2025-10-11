package com.example.mywebapp.entity;
import lombok.Data;

@Data
public class LinkUpdate {
    private String id;
    private String url;
    private String title;
    private String description;
    private String updated_by;
    private String category_id;
    private String status;
    private String is_active;
    private String shared;
}
