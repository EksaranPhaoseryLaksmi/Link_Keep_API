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

import java.time.LocalDateTime;
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

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        User user = userMapper.findByRememberToken(token);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token."));
        }

        // Check expiration (e.g., 30 minutes)
        if (user.getReset_token_created_at().plusMinutes(30).isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token has expired."));
        }

        // Update password
        String hashedNewPwd = passwordEncoder.encode(newPassword);
        user.setPassword(hashedNewPwd);
        userMapper.updatePassword(user);

        // Invalidate token
        userMapper.updateRememberTokenByEmail(user.getEmail(), null, null);

        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully."));
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
        userMapper.updateRememberTokenByEmail(email, token,LocalDateTime.now());

        // Send email
        String verificationLink = "http://localhost:8080/api/auth" + "/api/verify?token=" + token;
        sendReVerificationEmail(email, verificationLink);

        return ResponseEntity.ok(Map.of("message", "Verification email resent. Please check your inbox."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> resetPasswordVerification(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        User user = userMapper.findByEmail(email);

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No account found with that email."));
        }
        if (user.getReset_token_created_at() != null &&
                user.getReset_token_created_at().plusMinutes(5).isAfter(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("error", "You can only request a reset once every 5 minutes."));
        }
        String token = UUID.randomUUID().toString();
        userMapper.updateRememberTokenByEmail(email, token, LocalDateTime.now());

        //String resetPasswordLink = "http://localhost:8080/api/auth/reset-password?token=" + token;
        String resetPasswordLink ="http://localhost:8080/reset-password.html?token=" + token;
        sendResetPasswordEmail(email, resetPasswordLink);

        return ResponseEntity.ok(Map.of("message", "Reset Password email sent. Please check your inbox."));
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

    public void sendResetPasswordEmail(String toEmail, String resetPasswordLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Reset Password for Your Account");
            message.setText("Hello,\n\nPlease verify your email and reset password by clicking the link below:\n"
                    + resetPasswordLink + "\n\nThank you!");
            mailSender.send(message);
            System.out.println("✅ Reset Password email sent successfully to " + toEmail);
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
