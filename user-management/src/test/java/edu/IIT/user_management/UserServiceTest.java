package edu.IIT.user_management;

import edu.IIT.user_management.dto.UserDTO;
import edu.IIT.user_management.model.User;
import edu.IIT.user_management.repository.UserRepository;
import edu.IIT.user_management.service.UserServiceImpl;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService; // Your actual service class name

    private UserDTO userDTO;
    private User user;

    @BeforeEach
    void setUp() {
        // Initialize the mock objects and userDTO for testing
        userDTO = new UserDTO();
        userDTO.setEmail("testuser@example.com");
        userDTO.setPassword("password");

        user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
    }

    @Test
    void testRegisterUser_UserAlreadyExists() {
        // Mock userRepository to return a list containing a user with the same email
        List<User> existingUsers = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(existingUsers);
        when(modelMapper.map(userDTO, User.class)).thenReturn(user);

        // Call the service method
        ResponseEntity<?> response = userService.register(userDTO);

        // Assert that the response indicates the user already exists
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Username already exists");
    }

    @Test
    void testRegisterUser_UserDoesNotExist() {
        // Mock userRepository to return an empty list (no users in DB)
        when(userRepository.findAll()).thenReturn(Arrays.asList());
        when(modelMapper.map(userDTO, User.class)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Call the service method
        ResponseEntity<?> response = userService.register(userDTO);

        // Assert that the response indicates successful user creation
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("User created successfully");

        // Verify that password was encoded
        verify(passwordEncoder).encode(userDTO.getPassword());
        // Verify that user was saved in the repository
        verify(userRepository).save(user);
    }
}
