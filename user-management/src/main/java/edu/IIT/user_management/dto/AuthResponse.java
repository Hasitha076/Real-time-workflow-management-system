package edu.IIT.user_management.dto;

import edu.IIT.user_management.model.User;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthResponse {
    private String token;
    private UserDTO user;
}
