package edu.IIT.user_management.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OTPRequest {
    private String email;
    private int OTP;
}
