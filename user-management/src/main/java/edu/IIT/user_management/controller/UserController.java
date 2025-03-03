package edu.IIT.user_management.controller;

import edu.IIT.user_management.dto.UserDTO;
import edu.IIT.user_management.producer.UserProducer;
import edu.IIT.user_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

    private final UserService userService;
    private final UserProducer userProducer;

    @PostMapping("/createUser")
    public String addUser(@RequestBody UserDTO user) {
        userProducer.sendMessage(user);
        return userService.createUser(user);
    }

    @PutMapping("/updateUser")
    public String updateUser(@RequestBody UserDTO user) {
        System.out.println("User updated details: " + user);
        return userService.updateUser(user);
    }

    @DeleteMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return "User deleted successfully";
    }

    @GetMapping("/getUser/{id}")
    public UserDTO getUser(@PathVariable int id) {

        return userService.getUserById(id);
    }

    @GetMapping("/getAllUsers")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }


    @GetMapping("/filterUsers")
    public List<String> filterUsers(@RequestParam List<Integer> ids) { // Ensure param matches WebClient
        return userService.filterUsers(ids);
    }

    @GetMapping("/filterUsersDetails")
    public List<UserDTO> filterUsersDetails(@RequestParam List<Integer> ids) { // Ensure param matches WebClient
        return userService.filterUsersDetails(ids);
    }

    @GetMapping("/filterUserNames")
    public List<String> filterUserNames(@RequestParam List<Integer> ids) { // Ensure param matches WebClient
        return userService.filterUserNames(ids);
    }

}
