package edu.IIT.user_management.controller;

import edu.IIT.user_management.dto.*;
import edu.IIT.user_management.model.User;
import edu.IIT.user_management.producer.UserProducer;
import edu.IIT.user_management.service.UserService;
import edu.IIT.user_management.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(userService.login(authRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO user) {
        String result = userService.register(user);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        return ResponseEntity.ok(userService.logout(token));
    }

    @PostMapping("/generateOTP")
    public ResponseEntity<?> generateOTP(@RequestBody EmailRequest emailRequest) {
        return ResponseEntity.ok(userService.generateOTP(emailRequest));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO resetPasswordRequestDTO) {
        return ResponseEntity.ok(userService.resetPassword(resetPasswordRequestDTO));
    }



}
