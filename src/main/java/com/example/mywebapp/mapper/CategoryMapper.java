package com.example.mywebapp.mapper;

import com.example.mywebapp.entity.Category;
import com.example.mywebapp.entity.CategoryRequest;
import com.example.mywebapp.entity.CategoryUpdate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    List<Category> findAll(Long id);
    void insert(CategoryRequest request);
    void update(CategoryUpdate request);
}
