package com.example.mywebapp.entity;
import lombok.Data;

@Data
public class Link {
    private String url;
    private String title;
    private String describtion;
    private String created_by;
    private String category_id;
    private String id;
    private String status;
    private String shared;
    private String created_date;
}
