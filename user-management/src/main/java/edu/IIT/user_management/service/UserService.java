package edu.IIT.user_management.service;

import edu.IIT.user_management.dto.UserDTO;
import edu.IIT.user_management.model.User;

import java.util.List;

public interface UserService {

    public String createUser(UserDTO user);
    public String registerUser(UserDTO user);
    public UserDTO getUserById(int id);
    public String updateUser(UserDTO user);
    public void deleteUser(int id);
    public List<UserDTO> getAllUsers();
    public List<String> filterUsers(List<Integer> userIds);
    public List<UserDTO> filterUsersDetails(List<Integer> userIds);
    public List<String> filterUserNames(List<Integer> userIds);
    public UserDTO findByEmail(String email);
    public boolean checkPassword(String rawPassword, String encodedPassword);
}
