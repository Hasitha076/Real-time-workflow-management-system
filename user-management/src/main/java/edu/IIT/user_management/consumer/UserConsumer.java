package edu.IIT.user_management.consumer;

import edu.IIT.user_management.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserConsumer {

    private final UserService userService;

//    @KafkaListener(topics = "task-events", groupId = "user-management")
//    public void consumeUser(Map<String, Object> message) {
//        try {
//            assert message != null;
//            log.info(String.format("#### -> Consumed message -> %s", message.get("collaboratorIds")));
//        } catch (Exception e) {
//            log.error("Error consuming message", e);
//        }
//    }
}
