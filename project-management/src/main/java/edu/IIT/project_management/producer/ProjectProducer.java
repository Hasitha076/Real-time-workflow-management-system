package edu.IIT.project_management.producer;

import edu.IIT.project_management.config.ProjectTopicConfig;
import edu.IIT.project_management.dto.ProjectDTO;
import edu.IIT.project_management.dto.ProjectEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectProducer {

    private final KafkaTemplate<ProjectDTO, ProjectEventDTO> kafkaTemplate;

    public void sendMessage(String projectName, List<Integer> collaboratorIds) {
        log.info(String.format("#### -> Producing message -> %s", projectName, collaboratorIds));

        ProjectEventDTO projectEventDTO = new ProjectEventDTO(projectName, collaboratorIds);
        kafkaTemplate.send("project-events", projectEventDTO);
    }
}
