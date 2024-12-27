package edu.IIT.task_management.producer;

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
    private final KafkaTemplate<String, TaskCreateEventDTO> kafkaTemplateCreate;
    private final KafkaTemplate<String, TaskUpdateEventDTO> kafkaTemplateUpdate;
    private final KafkaTemplate<String, TaskDeleteEventDTO> kafkaTemplateDelete;

    public void sendMessage(TaskDTO taskDTO) {
        log.info(String.format("#### -> Producing message -> %s", taskDTO));
        kafkaTemplate.send("task-events", taskDTO);
    }

    public void sendCreateTaskMessage(String taskName, List<Integer> collaboratorIds) {
        log.info(String.format("#### -> Producing message -> %s", taskName, collaboratorIds));

        TaskCreateEventDTO taskDTO = new TaskCreateEventDTO(taskName, collaboratorIds);
        kafkaTemplateCreate.send("task-create-events", taskDTO);
    }

    public void sendUpdateTaskMessage(String taskName, String collaboratorAssignmentType, List<Integer> collaboratorIds) {
        log.info(String.format("#### -> Producing message -> %s", taskName, collaboratorIds));

        TaskUpdateEventDTO taskDTO = new TaskUpdateEventDTO(taskName, collaboratorAssignmentType, collaboratorIds);
        kafkaTemplateUpdate.send("task-update-events", taskDTO);
    }

    public void sendDeleteTaskMessage(String taskName, List<Integer> collaboratorIds) {
        log.info(String.format("#### -> Producing message -> %s", taskName, collaboratorIds));

        TaskDeleteEventDTO taskDTO = new TaskDeleteEventDTO(taskName, collaboratorIds);
        kafkaTemplateDelete.send("task-delete-events", taskDTO);
    }
}
