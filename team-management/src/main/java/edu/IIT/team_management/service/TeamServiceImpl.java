package edu.IIT.team_management.service;

import edu.IIT.team_management.dto.TeamDTO;
import edu.IIT.team_management.model.Team;
import edu.IIT.team_management.producer.TeamProducer;
import edu.IIT.team_management.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final ModelMapper modelMapper;
    private final TeamProducer teamProducer;

    @Override
    public String createTeam(TeamDTO teamDTO) {
        teamRepository.save(modelMapper.map(teamDTO, Team.class));
        teamProducer.sendCreatedTeamMessage(teamDTO.getTeamName(), teamDTO.getAssignerId(), teamDTO.getCollaboratorIds());
        return "Team created successfully";
    }

    @Override
    public TeamDTO getTeamById(int id) {
        return modelMapper.map(teamRepository.findById(id), TeamDTO.class);
    }

    @Override
    public String updateTeam(TeamDTO teamDTO) {
        System.out.println("Team updated");
        Optional<Team> team = teamRepository.findById(teamDTO.getTeamId());

        System.out.println("Team: " + team);
        if (team.isEmpty()) {
            return "Team not found";
        }

        teamDTO.setUpdatedAt(team.get().getUpdatedAt()); // Retain the updatedAt value from the old project
        teamDTO.setTeamDescription(team.get().getTeamDescription()); // Retain the teamDescription value from the old project
        teamDTO.setTeamName(team.get().getTeamName()); // Retain the teamName value from the old project
        teamDTO.setCreatedAt(team.get().getCreatedAt());
        teamDTO.setAssignerId(team.get().getAssignerId());
        teamDTO.setTags(team.get().getTags());

        List<Integer> oldCollaboratorIds = team.get().getCollaboratorIds(); // Get existing collaborators

        // Identify new collaborators: present in new project but not in old project
        List<Integer> newCollaborators = teamDTO.getCollaboratorIds().stream()
                .filter(collaboratorId -> !oldCollaboratorIds.contains(collaboratorId))
                .collect(Collectors.toList());

        // Identify removed collaborators: present in old project but not in new project
        List<Integer> removedCollaborators = oldCollaboratorIds.stream()
                .filter(collaboratorId -> !teamDTO.getCollaboratorIds().contains(collaboratorId))
                .collect(Collectors.toList());

        // Identify unchanged collaborators: present in both old and new project
        List<Integer> unchangedCollaborators = oldCollaboratorIds.stream()
                .filter(teamDTO.getCollaboratorIds()::contains)
                .collect(Collectors.toList());

        // Perform the update in the repository
        teamRepository.save(modelMapper.map(teamDTO, new TypeToken<Team>(){}.getType()));

        // Handle sending collaborator changes to the producer or further actions
        if (!newCollaborators.isEmpty()) {
            // Send newly added collaborators
            teamProducer.sendUpdateTeamMessage(teamDTO.getTeamName(), teamDTO.getAssignerId(),"New", newCollaborators);
        }

        if (!removedCollaborators.isEmpty()) {
            // Send removed collaborators
            teamProducer.sendUpdateTeamMessage(teamDTO.getTeamName(), teamDTO.getAssignerId(),"Removed", removedCollaborators);
        }

        // Optionally, you can also send unchanged collaborators if needed
        if (!unchangedCollaborators.isEmpty()) {
            // Handle unchanged collaborators if necessary
            teamProducer.sendUpdateTeamMessage(teamDTO.getTeamName(), teamDTO.getAssignerId(),"Existing", unchangedCollaborators);
        }

        return "Team updated successfully";
    }


    @Override
    public void deleteTeam(int id) {
        TeamDTO team = getTeamById(id);
        teamRepository.deleteById(id);
        teamProducer.sendDeleteTeamMessage(team.getTeamId(), team.getTeamName(), team.getAssignerId(), team.getCollaboratorIds());
    }

    @Override
    public List<TeamDTO> getAllTeams() {
        return modelMapper.map(teamRepository.findAll(), new TypeToken<List<TeamDTO>>(){}.getType());
    }

    @Override
    public List<String> filterTeams(List<Integer> teamIds) {
        List<Team> teamList = teamRepository.findAllById(teamIds);

        if (teamList.isEmpty()) {
            log.warn("No users found for IDs: {}", teamIds);
        }

        return teamList.stream().map(Team::getTeamName).toList();
    }

}
