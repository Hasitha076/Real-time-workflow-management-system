package edu.IIT.task_management.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.IIT.task_management.dto.TaskCreateEventDTO;
import edu.IIT.task_management.dto.TaskDTO;
import edu.IIT.task_management.dto.TaskDeleteEventDTO;
import edu.IIT.task_management.dto.TaskUpdateEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskProducer {

    private final KafkaTemplate<TaskDTO, TaskDTO> kafkaTemplate;
//    private final KafkaTemplate<String, TaskCreateEventDTO> kafkaTemplateCreate;
//    private final KafkaTemplate<String, TaskUpdateEventDTO> kafkaTemplateUpdate;
//    private final KafkaTemplate<String, TaskDeleteEventDTO> kafkaTemplateDelete;

    private final KafkaTemplate<String, String> kafkaTemplateCreate;
    private final KafkaTemplate<String, String> kafkaTemplateUpdate;
    private final KafkaTemplate<String, String> kafkaTemplateDelete;

    public void sendMessage(TaskDTO taskDTO) {
        log.info(String.format("#### -> Producing message -> %s", taskDTO));
        kafkaTemplate.send("task-events", taskDTO);
    }

//    public void sendCreateTaskMessage(String taskName, int assigneeId, List<Integer> collaboratorIds) {
//        log.info(String.format("#### -> Producing message -> %s", taskName, collaboratorIds));
//
//        TaskCreateEventDTO taskDTO = new TaskCreateEventDTO(taskName, assigneeId, collaboratorIds);
//        kafkaTemplateCreate.send("task-create-events", taskDTO);
//    }

    public void sendCreateTaskMessage(String taskName, int assigneeId, List<Integer> collaboratorIds) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TaskCreateEventDTO event = new TaskCreateEventDTO(taskName, assigneeId, collaboratorIds);

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplateCreate.send("task-create-events", message);

            log.info("Sent message: {}", message);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }

//    public void sendUpdateTaskMessage(String taskName, int assignerId, String collaboratorAssignmentType, List<Integer> collaboratorIds) {
//        log.info(String.format("#### -> Producing message -> %s", taskName, collaboratorIds));
//
//        TaskUpdateEventDTO taskDTO = new TaskUpdateEventDTO(taskName, assignerId, collaboratorAssignmentType, collaboratorIds);
//        kafkaTemplateUpdate.send("task-update-events", taskDTO);
//    }

    public void sendUpdateTaskMessage(String taskName, int assignerId, String collaboratorAssignmentType, List<Integer> collaboratorIds) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TaskUpdateEventDTO event = new TaskUpdateEventDTO(taskName, assignerId, collaboratorAssignmentType, collaboratorIds);

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplateUpdate.send("task-update-events", message);

            log.info("Sent message: {}", message);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }

//    public void sendDeleteTaskMessage(String taskName, int assignerId, List<Integer> collaboratorIds) {
//        log.info(String.format("#### -> Producing delete message -> %s", taskName, assignerId, collaboratorIds));
//
//        TaskDeleteEventDTO taskDTO = new TaskDeleteEventDTO(taskName, assignerId, collaboratorIds);
//        kafkaTemplateDelete.send("task-delete-events", taskDTO);
//    }

    public void sendDeleteTaskMessage(String taskName, int assignerId, List<Integer> collaboratorIds) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TaskDeleteEventDTO event = new TaskDeleteEventDTO(taskName, assignerId, collaboratorIds);

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplateDelete.send("task-delete-events", message);

            log.info("Sent message: {}", message);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }
}
