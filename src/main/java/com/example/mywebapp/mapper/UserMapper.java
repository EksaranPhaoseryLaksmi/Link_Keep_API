package com.example.mywebapp.mapper;

import com.example.mywebapp.entity.ChangePasswordRequest;
import com.example.mywebapp.entity.User;
import com.example.mywebapp.entity.UserRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {
    User findByUsername(String username);
    Boolean updatePassword(User request);
    void insertUser(UserRequest user);
    User findByRememberToken(@Param("token") String token);
    void verifyUser(@Param("token") String token);
    User findByEmail(@Param("email") String email);
    void updateRememberTokenByEmail(@Param("email") String email, @Param("token") String token,@Param("createdAt") LocalDateTime createdAt);
}
