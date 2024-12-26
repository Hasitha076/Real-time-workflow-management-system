package edu.IIT.task_management.consumer;

import edu.IIT.project_management.dto.ProjectDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskConsumer {

    @KafkaListener(topics = "user-events", groupId = "task-management")
    public void consumeUser(String message) {
        try {
            assert message != null;
            log.info(String.format("#### -> Consumed message -> %s", message));
        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }
}
