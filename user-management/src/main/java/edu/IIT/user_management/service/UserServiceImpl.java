package edu.IIT.user_management.service;

import edu.IIT.user_management.dto.UserDTO;
import edu.IIT.user_management.model.User;
import edu.IIT.user_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public String createUser(UserDTO user) {
        userRepository.save(modelMapper.map(user, User.class));
        return "User created successfully";
    }

    public UserDTO getUserById(int id) {
        return modelMapper.map(userRepository.findById(id), UserDTO.class);
    }

    public String updateUser(UserDTO userDTO) {
        Optional<User> user = (userRepository.findById(userDTO.getUserId()));
        if (user.isEmpty()) {
            return "User not found";
        }
        userDTO.setCreatedAt(user.get().getCreatedAt());
        userRepository.save(modelMapper.map(userDTO, new TypeToken<User>(){}.getType()));
        return "User updated successfully";
    }

    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    public List<UserDTO> getAllUsers() {
        return modelMapper.map(userRepository.findAll(), new TypeToken<List<UserDTO>>(){}.getType());
    }

}
