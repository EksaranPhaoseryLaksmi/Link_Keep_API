package com.example.mywebapp.controller;

import com.example.mywebapp.entity.*;
import com.example.mywebapp.service.CategoryService;
import com.example.mywebapp.service.LinkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/link")
public class LinkController {
    private final LinkService linkService;
    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping
    public List<Link> getAll() {
        return linkService.getAllLinks();
    }

    @PostMapping
    public void create(@RequestBody LinkRequest request) {
        linkService.addLink(request);
    }

    @PutMapping
    public void update(@RequestBody LinkUpdate request){
        linkService.updateLink(request);
    }
}
