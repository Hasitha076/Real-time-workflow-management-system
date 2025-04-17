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

    private final ModelMapper modelMapper;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserProducer userProducer;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {

        UserDTO user = userService.findByEmail(authRequest.getEmail());

        System.out.println("Auth request: " + authRequest);
        System.out.println("User: " + user);

        if (user != null && !userService.checkPassword(authRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(new AuthResponse("Invalid credentials", null));
        }

        String token = jwtUtil.generateToken(user.getUserName());

        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO user) {
        System.out.println("User ===> " + user);
        UserDTO existingUser = userService.findByEmail(user.getEmail());

        if (existingUser != null) {
            return ResponseEntity.status(400).body("Username already exists");
        }
        userService.registerUser(user);
        return ResponseEntity.ok("User created successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid token format");
        }

        String jwt = token.substring(7); // Remove 'Bearer ' prefix
        jwtUtil.blacklistToken(jwt); // Assuming this method exists for token invalidation

        return ResponseEntity.ok("Logged out successfully");
    }

    private final SecureRandom random = new SecureRandom();

    @PostMapping("/generateOTP")
    public ResponseEntity<?> generateOTP(@RequestBody EmailRequest emailRequest) {

        String email = emailRequest.getEmail();
        List<User> userList = modelMapper.map(userService.getAllUsers(), new TypeToken<List<User>>() {}.getType());
        Boolean checkUser = userList.stream().filter(user -> user.getEmail().equals(email)).anyMatch(user -> user.getEmail().equals(email));

        if (!checkUser) {
            System.out.println("User not found");
            return ResponseEntity.ok("User not found");
        } else {
            int otp = 10000 + random.nextInt(90000);

            System.out.println("Generated OTP for: " + otp);
            System.out.println(emailRequest.getEmail());

            OTPRequest otpRequest = new OTPRequest(emailRequest.getEmail(), otp);

            userProducer.sendOTPMessage(otpRequest);
            return ResponseEntity.ok(otp);
        }

    }


    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO resetPasswordRequestDTO) {
        String email = resetPasswordRequestDTO.getEmail();
        String password = resetPasswordRequestDTO.getPassword();

        UserDTO user = userService.findByEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userService.resetPassword(user);
        return ResponseEntity.ok("Password reset successfully");
    }



}
