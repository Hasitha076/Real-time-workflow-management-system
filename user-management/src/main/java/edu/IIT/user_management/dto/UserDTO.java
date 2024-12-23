package edu.IIT.user_management.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class UserDTO {
    private int userId;
    private String userName;
    private String email;
    private String password;
    private UserRole role;
    private boolean status = true;
    private String createdAt;
    private String updatedAt;

}
