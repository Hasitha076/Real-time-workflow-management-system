package edu.IIT.work_management.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.IIT.project_management.dto.ProjectDeleteEventDTO;
import edu.IIT.work_management.service.WorkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkConsumer {

    private final WorkService workService;

//    @KafkaListener(topics = "user-events", groupId = "task-management")
//    public void consumeUser(String message) {
//        try {
//            assert message != null;
//            log.info(String.format("#### -> Consumed message -> %s", message));
//        } catch (Exception e) {
//            log.error("Error consuming message", e);
//        }
//    }

    @KafkaListener(topics = "project-delete-events", groupId = "work-management")
    public void consumeProject(String message) {
        try {
            assert message != null;

            ObjectMapper objectMapper = new ObjectMapper();
            ProjectDeleteEventDTO projectDeleteEventDTO = objectMapper.readValue(message, ProjectDeleteEventDTO.class);

            log.info(String.format("#### -> Consumed project delete message -> %s", message));
            workService.deleteByProjectId(projectDeleteEventDTO.getProjectId());

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

}
