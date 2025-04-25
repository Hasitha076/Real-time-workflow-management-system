package edu.IIT.team_management.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.IIT.team_management.dto.TeamDTO;
import edu.IIT.team_management.dto.TeamCreateEventDTO;
import edu.IIT.team_management.dto.TeamDeleteEventDTO;
import edu.IIT.team_management.dto.TeamUpdateEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamProducer {

    private final KafkaTemplate<String, String> kafkaTemplateCreate;
    private final KafkaTemplate<String, String> kafkaTemplateUpdate;
    private final KafkaTemplate<String, String> kafkaTemplateDelete;

    public void sendCreatedTeamMessage(String TeamName, int assignerId, List<Integer> collaboratorIds) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TeamCreateEventDTO event = new TeamCreateEventDTO(TeamName, assignerId, collaboratorIds);

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplateCreate.send("team-create-events", message);

            log.info("Sent message: {}", message);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }

    public void sendUpdateTeamMessage(String TeamName, int assignerId, String collaboratorAssignmentType, List<Integer> collaboratorIds) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TeamUpdateEventDTO event = new TeamUpdateEventDTO(TeamName, assignerId, collaboratorAssignmentType, collaboratorIds);

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplateUpdate.send("team-update-events", message);

            log.info("Sent message: {}", message);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }

    public void sendDeleteTeamMessage(int teamId, String TeamName, int assignerId, List<Integer> collaboratorIds) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TeamDeleteEventDTO event = new TeamDeleteEventDTO(teamId, TeamName, assignerId, collaboratorIds);

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplateDelete.send("team-delete-events", message);

            log.info("Sent message: {}", message);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }
}
