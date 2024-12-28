package edu.IIT.project_management.producer;

import edu.IIT.project_management.dto.ProjectDTO;
import edu.IIT.project_management.dto.ProjectCreateEventDTO;
import edu.IIT.project_management.dto.ProjectDeleteEventDTO;
import edu.IIT.project_management.dto.ProjectUpdateEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectProducer {

    private final KafkaTemplate<ProjectDTO, ProjectCreateEventDTO> kafkaTemplateCreate;
    private final KafkaTemplate<ProjectDTO, ProjectUpdateEventDTO> kafkaTemplateUpdate;
    private final KafkaTemplate<ProjectDTO, ProjectDeleteEventDTO> kafkaTemplateDelete;

    public void sendCreatedProjectMessage(String projectName, int assignerId, List<Integer> collaboratorIds) {
        log.info(String.format("#### -> Producing message -> %s", projectName, assignerId, collaboratorIds));

        ProjectCreateEventDTO projectCreateEventDTO = new ProjectCreateEventDTO(projectName, assignerId, collaboratorIds);
        kafkaTemplateCreate.send("project-create-events", projectCreateEventDTO);
    }

    public void sendUpdateProjectMessage(String projectName, int assignerId, String collaboratorAssignmentType, List<Integer> collaboratorIds) {
        log.info(String.format("#### -> Producing message -> %s", projectName, collaboratorIds));

        ProjectUpdateEventDTO projectCreateEventDTO = new ProjectUpdateEventDTO(projectName, assignerId, collaboratorAssignmentType, collaboratorIds);
        kafkaTemplateUpdate.send("project-update-events", projectCreateEventDTO);
    }

    public void sendDeleteProjectMessage(String projectName, int assignerId, List<Integer> collaboratorIds) {
        log.info(String.format("#### -> Producing message -> %s", projectName, collaboratorIds));

        ProjectDeleteEventDTO projectDeleteEventDTO = new ProjectDeleteEventDTO(projectName, assignerId, collaboratorIds);
        kafkaTemplateDelete.send("project-delete-events", projectDeleteEventDTO);
    }
}
