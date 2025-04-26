package edu.IIT.user_management;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import edu.IIT.user_management.dto.*;
import edu.IIT.user_management.model.User;
import edu.IIT.user_management.producer.UserProducer;
import edu.IIT.user_management.repository.UserRepository;

import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.util.*;

import edu.IIT.user_management.service.UserServiceImpl;
import edu.IIT.user_management.utils.JwtUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.modelmapper.TypeToken;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SecureRandom random;

    @Mock
    private UserProducer userProducer;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setEmail("hasitha@gmail.com");

        userDTO = new UserDTO();
        userDTO.setEmail("hasitha@gmail.com");
        userDTO.setPassword("password123");
    }

    @Test
    public void testRegister_UserAlreadyExists() {
        List<User> existingUsers = new ArrayList<>();
        existingUsers.add(user);

        when(userRepository.findAll()).thenReturn(existingUsers);
        when(modelMapper.map(existingUsers, new TypeToken<List<User>>() {}.getType())).thenReturn(existingUsers);

        String result = userService.register(userDTO);

        assertThat(result).isEqualTo("Username already exists");
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    public void testRegister_NewUser() {
        // UserDTO input
        userDTO.setEmail("hasitha@gmail.com");
        userDTO.setPassword("Hasitha@123");

        // No users with this email exist
        List<User> existingUsers = new ArrayList<>();
        when(userRepository.findAll()).thenReturn(existingUsers);
        when(modelMapper.map(existingUsers, new TypeToken<List<User>>() {}.getType())).thenReturn(existingUsers);

        // Password encoding
        when(passwordEncoder.encode("Hasitha@123")).thenReturn("encodedpassword");

        // Mapping DTO to Entity
        User mappedUser = new User();
        mappedUser.setEmail("hasitha@gmail.com");
        mappedUser.setPassword("encodedpassword");

        when(modelMapper.map(userDTO, User.class)).thenReturn(mappedUser);

        // Call the method
        String result = userService.register(userDTO);

        // Assertions
        assertThat(result).isEqualTo("User created successfully");
        verify(userRepository).save(mappedUser);
    }

    @Test
    public void testLogin_Success() {
        // Prepare AuthRequest
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("hasitha@gmail.com");
        authRequest.setPassword("Hasitha@123");

        // Simulate user found in DB
        User mockUser = new User();
        mockUser.setEmail("hasitha@gmail.com");
        mockUser.setPassword("encoded-password");
        mockUser.setUserName("Hasitha");

        List<User> allUsers = List.of(mockUser);
        when(userRepository.findAll()).thenReturn(allUsers);
        when(userRepository.findByEmail("hasitha@gmail.com")).thenReturn(mockUser);

        // Map User to UserDTO
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("hasitha@gmail.com");
        userDTO.setPassword("encoded-password");
        userDTO.setUserName("Hasitha");

        when(modelMapper.map(mockUser, UserDTO.class)).thenReturn(userDTO);

        // Simulate password matches
        when(userService.checkPassword("Hasitha@123", "encoded-password")).thenReturn(true);

        // Simulate token generation
        when(jwtUtil.generateToken("Hasitha")).thenReturn("mock-jwt-token");

        // Call login method
        ResponseEntity<?> response = userService.login(authRequest);

        // Assert result
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(((AuthResponse) response.getBody()).getToken()).isEqualTo("mock-jwt-token");
        assertThat(((AuthResponse) response.getBody()).getUser().getEmail()).isEqualTo("hasitha@gmail.com");

        // Verify interactions
        verify(userRepository).findAll();
        verify(userRepository).findByEmail("hasitha@gmail.com");
        verify(jwtUtil).generateToken("Hasitha");
    }


    @Test
    public void testLogin_InvalidCredentials() {
        // Arrange
        String email = "hasitha@gmail.com";
        String rawPassword = "wrongPassword";
        String encodedPassword = "$2a$10$encrypted";

        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail(email);
        authRequest.setPassword(rawPassword);

        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setPassword(encodedPassword);
        mockUser.setUserName("Hasitha");

        List<User> allUsers = List.of(mockUser);
        when(userRepository.findAll()).thenReturn(allUsers);
        when(userRepository.findByEmail(email)).thenReturn(mockUser);

        UserDTO mockUserDTO = new UserDTO();
        mockUserDTO.setEmail(email);
        mockUserDTO.setPassword(encodedPassword);
        mockUserDTO.setUserName("Hasitha");

        when(modelMapper.map(mockUser, UserDTO.class)).thenReturn(mockUserDTO);
        when(userService.checkPassword(rawPassword, encodedPassword)).thenReturn(false);

        // Act
        ResponseEntity<?> response = userService.login(authRequest);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Invalid credentials", response.getBody());

        // Verify interactions
        verify(userRepository).findAll();
        verify(userRepository).findByEmail(email);
    }



    @Test
    public void testCheckPassword_ValidMatch() {
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$abcdefghijklmnopqrstuv"; // example bcrypt hash

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        boolean result = userService.checkPassword(rawPassword, encodedPassword);

        assertTrue(result);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    public void testCheckPassword_InvalidMatch() {
        String rawPassword = "wrongPassword";
        String encodedPassword = "$2a$10$abcdefghijklmnopqrstuv";

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        boolean result = userService.checkPassword(rawPassword, encodedPassword);

        assertFalse(result);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }


    @Test
    public void testLogout_ValidToken() {
        String bearerToken = "Bearer valid.jwt.token";

        String result = userService.logout(bearerToken);

        assertEquals("Logged out successfully", result);
        verify(jwtUtil).blacklistToken("valid.jwt.token");
    }

    @Test
    public void testLogout_NullToken() {
        String result = userService.logout(null);

        assertEquals("Invalid token format", result);
        verify(jwtUtil, never()).blacklistToken(any());
    }

    @Test
    public void testLogout_InvalidFormatToken() {
        String invalidToken = "invalidTokenFormat";

        String result = userService.logout(invalidToken);

        assertEquals("Invalid token format", result);
        verify(jwtUtil, never()).blacklistToken(any());
    }

    //    Logout function
    private EmailRequest emailRequest;
    private List<User> allUsers;

    @BeforeEach
    public void setup() {
        emailRequest = new EmailRequest();
        emailRequest.setEmail("hasitha@gmail.com");

        allUsers = new ArrayList<>();
    }

    @Test
    public void testGenerateOTP_UserNotFound() {
        // Mock the findAll() method to return an empty list
        when(userRepository.findAll()).thenReturn(allUsers);

        // Call the service method
        ResponseEntity<?> response = userService.generateOTP(emailRequest);

        // Assertions
        assertEquals("User not found", response.getBody());
        verify(userProducer, never()).sendOTPMessage(any());
    }

    @Test
    public void testGenerateOTP_UserExists() {
        // Setup: Mocked dependencies and test data
        List<User> allUsers = new ArrayList<>();
        User mockUser = new User();
        mockUser.setEmail("hasitha@gmail.com");
        allUsers.add(mockUser);

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail("hasitha@gmail.com");

        // Mock repository to return the list with the mock user
        when(userRepository.findAll()).thenReturn(allUsers);

        // Mock random to always return 23456 (OTP = 10000 + 23456 = 33456)
        when(random.nextInt(90000)).thenReturn(23456);

        // Execute the method
        ResponseEntity<?> response = userService.generateOTP(emailRequest);

        // Verify response is correct
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.getBody() instanceof Integer);
        Assertions.assertEquals(33456, response.getBody());

        // Capture and verify the OTPRequest sent to the producer
        ArgumentCaptor<OTPRequest> otpCaptor = ArgumentCaptor.forClass(OTPRequest.class);
        verify(userProducer).sendOTPMessage(otpCaptor.capture());

        OTPRequest sentOtp = otpCaptor.getValue();
        Assertions.assertNotNull(sentOtp);
        Assertions.assertEquals("hasitha@gmail.com", sentOtp.getEmail());
        Assertions.assertEquals(33456, sentOtp.getOTP());
    }

    @Test
    public void testResetPassword_Success() {
        // Arrange
        String testEmail = "hasitha@gmail.com";
        String rawPassword = "newPassword123";
        String encodedPassword = "encodedPassword123";

        // Setup request DTO
        ResetPasswordRequestDTO requestDTO = new ResetPasswordRequestDTO();
        requestDTO.setEmail(testEmail);
        requestDTO.setPassword(rawPassword);

        // Mock existing user
        User mockUser = new User();
        mockUser.setEmail(testEmail);

        // Mock repository and encoder behavior
        when(userRepository.findByEmail(testEmail)).thenReturn(mockUser);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // Act
        ResponseEntity<?> response = userService.resetPassword(requestDTO);

        // Assert response
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Password reset successfully", response.getBody());

        // Assert user password update
        Assertions.assertEquals(encodedPassword, mockUser.getPassword());

        // Verify save was called with the updated user
        verify(userRepository).save(mockUser);
    }


    @Test
    public void testUpdateUser_Success() {
        // Arrange
        User existingUser = new User();
        existingUser.setUserId(1);
        existingUser.setEmail("hasitha@gmail.com");
        existingUser.setPassword("hashedPassword");

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(1);
        userDTO.setUserName("Updated Name");

        when(userRepository.findById(1)).thenReturn(Optional.of(existingUser));
        when(modelMapper.map(userDTO, new TypeToken<User>(){}.getType()))
                .thenReturn(new User());

        // Act
        String result = userService.updateUser(userDTO);

        // Assert
        Assertions.assertEquals("User updated successfully", result);

        // Verify save was called
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testDeleteUser_Success() {
        // Arrange
        int userId = 1;

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    public void testGetAllUsers_Success() {
        // Arrange
        User user1 = new User();
        user1.setUserId(1);
        user1.setUserName("Alice");
        user1.setEmail("alice@gmail.com");

        User user2 = new User();
        user2.setUserId(2);
        user2.setUserName("Bob");
        user2.setEmail("bob@gmail.com");

        List<User> userList = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(userList);

        UserDTO userDTO1 = new UserDTO();
        userDTO1.setUserId(1);
        userDTO1.setUserName("Alice");
        userDTO1.setEmail("alice@gmail.com");

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setUserId(2);
        userDTO2.setUserName("Bob");
        userDTO2.setEmail("bob@gmail.com");

        List<UserDTO> userDTOList = Arrays.asList(userDTO1, userDTO2);

        when(modelMapper.map(eq(userList), any(Type.class))).thenReturn(userDTOList);

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Alice", result.get(0).getUserName());
        Assertions.assertEquals("Bob", result.get(1).getUserName());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testFilterUsersDetails_WithValidUserIds() {
        // Arrange
        List<Integer> userIds = Arrays.asList(1, 2);

        User user1 = new User();
        user1.setUserId(1);
        user1.setUserName("Alice");

        User user2 = new User();
        user2.setUserId(2);
        user2.setUserName("Bob");

        List<User> userList = Arrays.asList(user1, user2);

        when(userRepository.findAllById(userIds)).thenReturn(userList);

        UserDTO userDTO1 = new UserDTO();
        userDTO1.setUserId(1);
        userDTO1.setUserName("Alice");

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setUserId(2);
        userDTO2.setUserName("Bob");

        when(modelMapper.map(user1, UserDTO.class)).thenReturn(userDTO1);
        when(modelMapper.map(user2, UserDTO.class)).thenReturn(userDTO2);

        // Act
        List<UserDTO> result = userService.filterUsersDetails(userIds);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Alice", result.get(0).getUserName());
        Assertions.assertEquals("Bob", result.get(1).getUserName());

        verify(userRepository).findAllById(userIds);
    }

}


