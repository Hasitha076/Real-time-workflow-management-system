package edu.IIT.user_management.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.IIT.user_management.dto.OTPRequest;
import edu.IIT.user_management.dto.UserDTO;
import edu.IIT.user_management.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProducer {

    private final KafkaTemplate<String, UserDTO> kafkaTemplate;
    private final KafkaTemplate<OTPRequest, OTPRequest> kafkaOTP;

    public void sendMessage(UserDTO userDTO) {
        log.info(String.format("#### -> Producing message -> %s", userDTO));
        kafkaTemplate.send("user-events", userDTO);
    }

    public void sendOTPMessage(OTPRequest otpRequest) {
        try {
            kafkaOTP.send("OTP-events", otpRequest);
            log.info("Sent email: ", otpRequest.getEmail());
            log.info("Sent OTP: {}", otpRequest.getOTP());
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }
}
