package edu.IIT.user_management.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthRequest {
    private String email;
    private String password;
}
