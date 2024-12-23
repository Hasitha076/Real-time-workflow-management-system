package edu.IIT.user_management.producer;

import edu.IIT.user_management.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProducer {

    private final KafkaTemplate<UserDTO, UserDTO> kafkaTemplate;

    public void sendMessage(UserDTO userDTO) {
        log.info(String.format("#### -> Producing message -> %s", userDTO));
        kafkaTemplate.send("user-events", userDTO);
    }
}
