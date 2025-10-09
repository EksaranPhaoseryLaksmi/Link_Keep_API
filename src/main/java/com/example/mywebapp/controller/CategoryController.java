package com.example.mywebapp.controller;
import com.example.mywebapp.entity.Category;
import com.example.mywebapp.entity.CategoryRequest;
import com.example.mywebapp.entity.CategoryUpdate;
import com.example.mywebapp.service.CategoryService;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/category")
public class CategoryController {
    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<Category> getAll() {
        return categoryService.getAllTeams();
    }

    @PostMapping
    public void create(@RequestBody CategoryRequest request) {
        categoryService.addCategory(request);
    }

    @PutMapping
    public void update(@RequestBody CategoryUpdate request){
        categoryService.updateCategory(request);
    }
}
