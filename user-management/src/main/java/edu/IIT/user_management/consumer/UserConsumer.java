package edu.IIT.user_management.consumer;

import edu.IIT.project_management.dto.ProjectDTO;
import edu.IIT.task_management.dto.TaskDTO;
import edu.IIT.user_management.dto.UserDTO;
import edu.IIT.user_management.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserConsumer {

    private final UserService userService;

    @KafkaListener(topics = "task-events", groupId = "user-management")
    public void consumeUser(TaskDTO message) {
        try {
            assert message != null;
            log.info(String.format("#### -> Consumed message -> %s", message.getCollaboratorIds()));
        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }
}
