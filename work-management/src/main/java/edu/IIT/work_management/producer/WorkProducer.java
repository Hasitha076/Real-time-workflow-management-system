package edu.IIT.work_management.producer;

import edu.IIT.project_management.dto.ProjectDeleteEventDTO;
import edu.IIT.work_management.dto.WorkCreateEventDTO;
import edu.IIT.work_management.dto.WorkDTO;
import edu.IIT.work_management.dto.WorkDeleteEventDTO;
import edu.IIT.work_management.dto.WorkUpdateEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkProducer {

    private final KafkaTemplate<WorkDTO, WorkDTO> kafkaTemplate;
    private final KafkaTemplate<String, WorkCreateEventDTO> kafkaTemplateCreate;
    private final KafkaTemplate<String, WorkUpdateEventDTO> kafkaTemplateUpdate;
    private final KafkaTemplate<String, WorkDeleteEventDTO> kafkaTemplateDelete;

    public void sendMessage(WorkDTO workDTO) {
        log.info(String.format("#### -> Producing message -> %s", workDTO));
        kafkaTemplate.send("task-events", workDTO);
    }

//    public void sendCreateTaskMessage(String taskName, int assigneeId, List<Integer> collaboratorIds) {
//        log.info(String.format("#### -> Producing message -> %s", taskName, collaboratorIds));
//
//        WorkCreateEventDTO taskDTO = new WorkCreateEventDTO(taskName, assigneeId, collaboratorIds);
//        kafkaTemplateCreate.send("task-create-events", taskDTO);
//    }

    public void sendUpdateWorkMessage(String workName, int assignerId, String collaboratorAssignmentType, List<Integer> collaboratorIds) {
        log.info(String.format("#### -> Producing work update message -> %s", workName, collaboratorIds));

        WorkUpdateEventDTO workDTO = new WorkUpdateEventDTO(workName, assignerId, collaboratorAssignmentType, collaboratorIds);
        kafkaTemplateUpdate.send("work-update-events", workDTO);
    }

    public void sendDeleteWorkMessage(int workId, String workName, int assignerId, List<Integer> collaboratorIds) {
        log.info(String.format("#### -> Producing work delete message -> %s", workName, collaboratorIds));

        WorkDeleteEventDTO workDeleteEventDTO = new WorkDeleteEventDTO(workId, workName, assignerId, collaboratorIds);
        kafkaTemplateDelete.send("work-delete-events", workDeleteEventDTO);
    }
}
