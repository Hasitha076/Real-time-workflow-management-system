package edu.IIT.user_management;

import edu.IIT.user_management.controller.UserController;
import edu.IIT.user_management.dto.UserDTO;
import edu.IIT.user_management.producer.UserProducer;
import edu.IIT.user_management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private UserProducer userProducer;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testAddUser() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName("John Doe");
        userDTO.setEmail("john@gmail.com");

        when(userService.createUser(any(UserDTO.class))).thenReturn("User created");

        mockMvc.perform(post("/api/v1/user/createUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("User created"));

        verify(userProducer).sendMessage(any(UserDTO.class));
    }

    @Test
    public void testUpdateUser() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(1);
        userDTO.setUserName("Updated Name");

        when(userService.updateUser(any(UserDTO.class))).thenReturn("User updated successfully");

        mockMvc.perform(put("/api/v1/user/updateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("User updated successfully"));
    }

    @Test
    public void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/v1/user/deleteUser/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));

        verify(userService).deleteUser(1);
    }

    @Test
    public void testGetUser() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(1);
        userDTO.setUserName("John Doe");

        when(userService.getUserById(1)).thenReturn(userDTO);

        mockMvc.perform(get("/api/v1/user/getUser/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.userName").value("John Doe"));
    }

    @Test
    public void testGetAllUsers() throws Exception {
        List<UserDTO> users = Collections.singletonList(new UserDTO(1, "Alice", "alice@gmail.com", null, null, true, null, null));

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/v1/user/getAllUsers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].userName").value("Alice"));
    }

    @Test
    public void testFilterUsersDetails() throws Exception {
        List<UserDTO> filtered = Collections.singletonList(new UserDTO(1, "Bob", "bob@gmail.com", null, null, true, null, null));

        when(userService.filterUsersDetails(Arrays.asList(1, 2))).thenReturn(filtered);

        mockMvc.perform(get("/api/v1/user/filterUsersDetails")
                        .param("ids", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].userName").value("Bob"));
    }

    @Test
    public void testFilterUserNames() throws Exception {
        List<String> names = Arrays.asList("Alice", "Bob");

        when(userService.filterUserNames(Arrays.asList(1, 2))).thenReturn(names);

        mockMvc.perform(get("/api/v1/user/filterUserNames")
                        .param("ids", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Alice"))
                .andExpect(jsonPath("$[1]").value("Bob"));
    }
}
