package edu.IIT.user_management.service;

import edu.IIT.user_management.dto.*;
import edu.IIT.user_management.model.User;
import edu.IIT.user_management.producer.UserProducer;
import edu.IIT.user_management.repository.UserRepository;
import edu.IIT.user_management.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SecureRandom random;
    private final UserProducer userProducer;

    @Override
    public UserDTO findByEmail(String email) {
        return modelMapper.map(userRepository.findByEmail(email), UserDTO.class);
    }

    @Override
    public String createUser(UserDTO user) {
        System.out.println("User: " + user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(modelMapper.map(user, User.class));
        return "User created successfully";
    }

    @Override
    public String register(UserDTO user) {

        List<User> userList = modelMapper.map(userRepository.findAll(), new TypeToken<List<User>>() {}.getType());
        Boolean checkUser = userList.stream().filter(ele -> ele.getEmail().equals(user.getEmail()))
                .anyMatch(ele -> ele.getEmail().equals(user.getEmail()));

        if (checkUser) {
            System.out.println("User already exists");
            return "Username already exists";
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(modelMapper.map(user, User.class));
            return "User created successfully";
        }

    }

    @Override
    public ResponseEntity<?> login(AuthRequest authRequest) {

        Boolean checkUser = userRepository.findAll().stream()
                .anyMatch(ele -> ele.getEmail().equals(authRequest.getEmail()));

        if (!checkUser) {
            return ResponseEntity.ok("User not found");
        }

        UserDTO user = modelMapper.map(userRepository.findByEmail(authRequest.getEmail()), UserDTO.class);

        if (checkPassword(authRequest.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user.getUserName());
            return ResponseEntity.ok(new AuthResponse(token, user));
        }
        else {
            return ResponseEntity.ok("Invalid credentials");
        }
    }

    @Override
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public String logout(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return "Invalid token format";
        }
        String jwt = token.substring(7);
        jwtUtil.blacklistToken(jwt);

        return "Logged out successfully";
    }

    @Override
    public ResponseEntity<?> generateOTP(EmailRequest emailRequest) {
        String email = emailRequest.getEmail();
        List<User> userList = userRepository.findAll();  // Directly using findAll() without mapping

        if (userList == null || userList.isEmpty()) {
            return ResponseEntity.ok("User not found");
        }

        Boolean checkUser = userList.stream().anyMatch(user -> user.getEmail().equals(email));

        if (!checkUser) {
            return ResponseEntity.ok("User not found");
        } else {
            int otp = 10000 + random.nextInt(90000);
            OTPRequest otpRequest = new OTPRequest(emailRequest.getEmail(), otp);
            userProducer.sendOTPMessage(otpRequest);
            return ResponseEntity.ok(otp);
        }
    }


    @Override
    public ResponseEntity<?> resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO) {
        String email = resetPasswordRequestDTO.getEmail();
        String password = resetPasswordRequestDTO.getPassword();

        User user = userRepository.findByEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return ResponseEntity.ok("Password reset successfully");
    }

    @Override
    public UserDTO getUserById(int id) {
        return modelMapper.map(userRepository.findById(id), UserDTO.class);
    }

    @Override
    public String updateUser(UserDTO userDTO) {
        Optional<User> user = (userRepository.findById(userDTO.getUserId()));
        if (user.isEmpty()) {
            return "User not found";
        }
        userDTO.setCreatedAt(user.get().getCreatedAt());
        userDTO.setEmail(user.get().getEmail());
        userDTO.setPassword(user.get().getPassword());
        userRepository.save(modelMapper.map(userDTO, new TypeToken<User>(){}.getType()));
        return "User updated successfully";
    }

//    @Override
//    public void resetPassword(UserDTO userDTO) {
//        userRepository.save(modelMapper.map(userDTO, new TypeToken<User>(){}.getType()));
//    }

    @Override
    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return modelMapper.map(userRepository.findAll(), new TypeToken<List<UserDTO>>(){}.getType());
    }

    @Override
    public List<UserDTO> filterUsersDetails(List<Integer> userIds) {
        List<User> userList = userRepository.findAllById(userIds);

        if (userList.isEmpty()) {
            log.warn("No users found for IDs: {}", userIds);
        }

        return userList.stream().map(user -> modelMapper.map(user, UserDTO.class)).toList();
    }

    @Override
    public List<String> filterUsers(List<Integer> userIds) {
        List<User> userList = userRepository.findAllById(userIds);

        if (userList.isEmpty()) {
            log.warn("No users found for IDs: {}", userIds);
        }

        return userList.stream().map(User::getEmail).toList();
    }

    @Override
    public List<String> filterUserNames(List<Integer> userIds) {
        List<User> userList = userRepository.findAllById(userIds);

        if (userList.isEmpty()) {
            log.warn("No users found for IDs: {}", userIds);
        }

        return userList.stream().map(User::getUserName).toList();
    }

}
