package com.example.mywebapp.controller;

import com.example.mywebapp.entity.User;
import com.example.mywebapp.entity.ChangePasswordRequest;
import com.example.mywebapp.entity.UserRequest;
import com.example.mywebapp.mapper.UserMapper;
import com.example.mywebapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCrypt;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    public AuthController(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User loginData) {
        User user = userMapper.findByUsername(loginData.getUsername());
        Map<String, Object> response = new HashMap<>();
        if (user != null && BCrypt.checkpw(loginData.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user.getUsername());
            response.put("token", token);
            response.put("user", user); // now allowed
        } else {
            response.put("error", "Invalid username or password");
        }
        return response;
    }

    @PostMapping("/register")
    public void adduser(@RequestBody UserRequest user)
    {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insertUser(user);
    }

    @PostMapping("/changepwd")
    public Map<String, Object> changePassword(@RequestBody ChangePasswordRequest req) {
        Map<String, Object> response = new HashMap<>();

        // 1. Get user from DB
        User user = userMapper.findByUsername(req.getUsername());
        if (user == null) {
            response.put("error", "User not found");
            return response;
        }

        // 2. Check old password
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            response.put("error", "Old password is incorrect");
            return response;
        }

        // 3. Check if new and confirm match
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            response.put("error", "New password and confirm password do not match");
            return response;
        }

        // 4. Encode new password and update
        String hashedNewPwd = passwordEncoder.encode(req.getNewPassword());
        user.setPassword(hashedNewPwd);
        userMapper.updatePassword(user); // implement in mapper

        response.put("message", "Password updated successfully");
        return response;
    }

}
