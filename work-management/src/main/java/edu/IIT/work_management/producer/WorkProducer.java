package edu.IIT.work_management.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.IIT.work_management.dto.WorkCreateEventDTO;
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

    private final KafkaTemplate<String, String> kafkaTemplateCreate;
    private final KafkaTemplate<String, String> kafkaTemplateUpdate;
    private final KafkaTemplate<String, String> kafkaTemplateDelete;



//    public void sendMessage(WorkDTO workDTO) {
//        log.info(String.format("#### -> Producing message -> %s", workDTO));
//        kafkaTemplate.send("task-events", workDTO);
//    }

    public void sendCreateWorkMessage(String workName, int assigneeId, List<Integer> collaboratorIds) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            WorkCreateEventDTO event = new WorkCreateEventDTO(workName, assigneeId, collaboratorIds);

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplateCreate.send("work-create-events", message);

            log.info("Sent message: {}", message);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }

    public void sendUpdateWorkMessage(String workName, int assignerId, String collaboratorAssignmentType, List<Integer> collaboratorIds) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            WorkUpdateEventDTO event = new WorkUpdateEventDTO(workName, assignerId, collaboratorAssignmentType, collaboratorIds);

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplateUpdate.send("work-update-events", message);

            log.info("Sent message: {}", message);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }

    public void sendDeleteWorkMessage(int workId, String workName, int assignerId, List<Integer> collaboratorIds) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            WorkDeleteEventDTO event = new WorkDeleteEventDTO(workId, workName, assignerId, collaboratorIds);

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplateDelete.send("work-delete-events", message);

            log.info("Sent message: {}", message);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }
}
