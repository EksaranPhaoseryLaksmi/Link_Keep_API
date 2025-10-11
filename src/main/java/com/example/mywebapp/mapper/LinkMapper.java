package com.example.mywebapp.mapper;

import com.example.mywebapp.entity.*;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface LinkMapper {
    List<Link> findAll();
    void insert(LinkRequest request);
    void update(LinkUpdate request);
}
