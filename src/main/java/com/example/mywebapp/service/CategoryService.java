package com.example.mywebapp.service;

import com.example.mywebapp.entity.Category;
import com.example.mywebapp.entity.CategoryRequest;
import com.example.mywebapp.entity.CategoryUpdate;
import com.example.mywebapp.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<Category> getAllTeams(Long id) {
        return categoryMapper.findAll(id);
    }

    public void addCategory(CategoryRequest request){
        categoryMapper.insert(request);
    };

    public void updateCategory(CategoryUpdate request){
      categoryMapper.update(request);
    };
}
