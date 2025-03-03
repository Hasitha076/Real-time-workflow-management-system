package edu.IIT.user_management.service;

import edu.IIT.user_management.dto.UserDTO;
import edu.IIT.user_management.model.User;
import edu.IIT.user_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO findByEmail(String email) {
        return modelMapper.map(userRepository.findByEmail(email), UserDTO.class);
    }

    @Override
    public String createUser(UserDTO user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(modelMapper.map(user, User.class));
        return "User created successfully";
    }

    @Override
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
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
