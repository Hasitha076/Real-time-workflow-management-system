package edu.IIT.task_management.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.IIT.project_management.dto.ProjectDeleteEventDTO;
import edu.IIT.task_management.service.TaskService;
import edu.IIT.work_management.dto.WorkDeleteEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskConsumer {

    private final TaskService taskService;

//    @KafkaListener(topics = "user-events", groupId = "task-management")
//    public void consumeUser(String message) {
//        try {
//            assert message != null;
//            log.info(String.format("#### -> Consumed message -> %s", message));
//        } catch (Exception e) {
//            log.error("Error consuming message", e);
//        }
//    }

    @KafkaListener(topics = "project-delete-events", groupId = "task-management")
    public void consumeProject(String message) {
        try {
            assert message != null;

            ObjectMapper objectMapper = new ObjectMapper();
            ProjectDeleteEventDTO projectDeleteEventDTO = objectMapper.readValue(message, ProjectDeleteEventDTO.class);

            log.info(String.format("#### -> Consumed message -> %s", message));
            taskService.deleteByProjectId(projectDeleteEventDTO.getProjectId());

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }
    }

//    @KafkaListener(topics = "work-delete-events", groupId = "task-management")
//    public void consumeWork(WorkDeleteEventDTO message) {
//        try {
//            assert message != null;
//            log.info(String.format("#### -> Consumed message -> %s", message));
//            taskService.deleteByProjectId(message.getProjectId());
//
//        } catch (Exception e) {
//            log.error("Error consuming message", e);
//        }
//    }

}
