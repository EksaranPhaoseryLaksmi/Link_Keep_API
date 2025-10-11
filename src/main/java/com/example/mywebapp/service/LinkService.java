package com.example.mywebapp.service;

import com.example.mywebapp.entity.*;
import com.example.mywebapp.mapper.CategoryMapper;
import com.example.mywebapp.mapper.LinkMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LinkService {

    private final LinkMapper linkMapper;

    public LinkService(LinkMapper linkMapper) {
        this.linkMapper = linkMapper;
    }

    public List<Link> getAllLinks() {
        return linkMapper.findAll();
    }

    public void addLink(LinkRequest request){
        linkMapper.insert(request);
    };

    public void updateLink(LinkUpdate request){
        linkMapper.update(request);
    };
}
