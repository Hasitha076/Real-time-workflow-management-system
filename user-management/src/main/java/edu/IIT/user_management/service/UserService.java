package edu.IIT.user_management.service;

import edu.IIT.user_management.dto.UserDTO;

import java.util.List;

public interface UserService {

    public String createUser(UserDTO user);
    public UserDTO getUserById(int id);
    public String updateUser(UserDTO user);
    public void deleteUser(int id);
    public List<UserDTO> getAllUsers();
}
