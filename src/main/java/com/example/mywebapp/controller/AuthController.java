package com.example.mywebapp.controller;

import com.example.mywebapp.entity.User;
import com.example.mywebapp.entity.ChangePasswordRequest;
import com.example.mywebapp.entity.UserRequest;
import com.example.mywebapp.mapper.UserMapper;
import com.example.mywebapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCrypt;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    public AuthController(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User loginData) {
        Map<String, Object> response = new HashMap<>();

        User user = userMapper.findByUsername(loginData.getUsername());
        if (user == null) {
            response.put("error", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (!BCrypt.checkpw(loginData.getPassword(), user.getPassword())) {
            response.put("error", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (user.getEmail_verified_at() == null) {
            response.put("error", "Please verify your email before logging in.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String token = jwtUtil.generateToken(user.getUsername());
        response.put("token", token);
        response.put("user", user);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/register")
    public Map<String, Object> adduser(@RequestBody UserRequest user)
    {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Set verification defaults
        user.setVerified(false);
        user.setRemember_token(UUID.randomUUID().toString());
        userMapper.insertUser(user);
        // Send verification email
        User user_new = userMapper.findByEmail(user.getUsername());
        sendVerificationEmail(user_new);

        return Map.of("message", "Please check your email to verify your account.");
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

    private void sendVerificationEmail(User user) {
        String verifyUrl = "http://localhost:8080/api/auth/verify?token=" + user.getRemember_token();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verify your email address");
        message.setText("Hello " + user.getName() + ",\n\nPlease verify your email by clicking the link below:\n" + verifyUrl);
        mailSender.send(message);
    }

    @GetMapping("/verify")
    public Map<String, Object> verifyUser(@RequestParam("token") String token) {
        User existing = userMapper.findByRememberToken(token);
        if (existing == null) {
            return Map.of("message", "Invalid or expired verification link.");
        }

        userMapper.verifyUser(token);
        return Map.of("message", "Email verified successfully. You can now log in.");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        User user = userMapper.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No account found with that email."));
        }

        if (user.getEmail_verified_at() != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already verified."));
        }

        // Generate new token
        String token = UUID.randomUUID().toString();
        userMapper.updateRememberTokenByEmail(email, token);

        // Send email
        String verificationLink = "http://localhost:8080/api/auth/verify?token=" + "/api/verify?token=" + token;
        sendReVerificationEmail(email, verificationLink);

        return ResponseEntity.ok(Map.of("message", "Verification email resent. Please check your inbox."));
    }

    public void sendReVerificationEmail(String toEmail, String verificationLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Verify Your Account");
            message.setText("Hello,\n\nPlease verify your email by clicking the link below:\n"
                    + verificationLink + "\n\nThank you!");
            mailSender.send(message);
            System.out.println("✅ Verification email sent successfully to " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }
    @PostMapping("/test-mail")
    public void sendTestEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("arsenalseng0401@gmail.com");
        message.setSubject("Test Email");
        message.setText("This is a test using Gmail App Password!");
        mailSender.send(message);
        System.out.println("Email sent!");
    }

}
