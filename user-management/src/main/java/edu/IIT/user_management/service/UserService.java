package edu.IIT.user_management.service;

import edu.IIT.user_management.dto.*;
import edu.IIT.user_management.model.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {

    public String createUser(UserDTO user);
    public String register(UserDTO user);
    public ResponseEntity<?> login(AuthRequest authRequest);
    public String logout(String token);
    public ResponseEntity<?> generateOTP(EmailRequest emailRequest);
    public String resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO);
    public UserDTO getUserById(int id);
    public String updateUser(UserDTO user);
    public void deleteUser(int id);
    public List<UserDTO> getAllUsers();
    public List<String> filterUsers(List<Integer> userIds);
    public List<UserDTO> filterUsersDetails(List<Integer> userIds);
    public List<String> filterUserNames(List<Integer> userIds);
    public UserDTO findByEmail(String email);
    public boolean checkPassword(String rawPassword, String encodedPassword);
    public void resetPassword(UserDTO userDTO);
}
