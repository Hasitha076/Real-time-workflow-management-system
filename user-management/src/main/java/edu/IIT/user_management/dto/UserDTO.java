package edu.IIT.user_management.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDTO {
    private int userId;
    private String userName;
    private String email;
    private String password;
    private UserRole role;
    private boolean status = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
