package edu.IIT.user_management.controller;

import edu.IIT.user_management.dto.AuthRequest;
import edu.IIT.user_management.dto.AuthResponse;
import edu.IIT.user_management.dto.UserDTO;
import edu.IIT.user_management.model.User;
import edu.IIT.user_management.service.UserService;
import edu.IIT.user_management.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private final ModelMapper modelMapper;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        System.out.println("Auth request: " + authRequest);
        UserDTO user = userService.findByEmail(authRequest.getEmail());

//        if (user != null || !userService.checkPassword(authRequest.getPassword(), user.getPassword())) {
//            return ResponseEntity.status(401).body("Invalid credentials");
//        }

        String token = jwtUtil.generateToken(user.getUserName());

        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO user) {
        System.out.println("User ===> " + user);
//        UserDTO existingUser = userService.findByEmail(user.getEmail());
//
//        if (existingUser != null) {
//            return ResponseEntity.status(400).body("Username already exists");
//        }
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


}
