package edu.IIT.team_management.producer;

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

    private final KafkaTemplate<TeamDTO, TeamCreateEventDTO> kafkaTemplateCreate;
    private final KafkaTemplate<TeamDTO, TeamUpdateEventDTO> kafkaTemplateUpdate;
    private final KafkaTemplate<TeamDTO, TeamDeleteEventDTO> kafkaTemplateDelete;

    public void sendCreatedTeamMessage(String TeamName, int assignerId, List<Integer> collaboratorIds) {
        log.info(String.format("#### -> Producing message -> %s", TeamName, assignerId, collaboratorIds));

        TeamCreateEventDTO teamCreateEventDTO = new TeamCreateEventDTO(TeamName, assignerId, collaboratorIds);
        kafkaTemplateCreate.send("team-create-events", teamCreateEventDTO);
    }

    public void sendUpdateTeamMessage(String TeamName, int assignerId, String collaboratorAssignmentType, List<Integer> collaboratorIds) {
        log.info(String.format("#### -> Producing message -> %s", TeamName, collaboratorIds));

        TeamUpdateEventDTO projectCreateEventDTO = new TeamUpdateEventDTO(TeamName, assignerId, collaboratorAssignmentType, collaboratorIds);
        kafkaTemplateUpdate.send("team-update-events", projectCreateEventDTO);
    }

    public void sendDeleteTeamMessage(int teamId, String TeamName, int assignerId, List<Integer> collaboratorIds) {
        log.info(String.format("#### -> Producing message -> %s", TeamName, collaboratorIds));

        TeamDeleteEventDTO teamDeleteEventDTO = new TeamDeleteEventDTO(teamId, TeamName, assignerId, collaboratorIds);
        kafkaTemplateDelete.send("team-delete-events", teamDeleteEventDTO);
    }
}
