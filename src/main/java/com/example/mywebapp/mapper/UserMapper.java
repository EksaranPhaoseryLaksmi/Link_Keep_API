package com.example.mywebapp.mapper;

import com.example.mywebapp.entity.ChangePasswordRequest;
import com.example.mywebapp.entity.User;
import com.example.mywebapp.entity.UserRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findByUsername(String username);
    Boolean updatePassword(User request);
    void insertUser(UserRequest user);
}
