package edu.IIT.team_management;

import edu.IIT.team_management.dto.TeamDTO;
import edu.IIT.team_management.model.Team;
import edu.IIT.team_management.producer.TeamProducer;
import edu.IIT.team_management.repository.TeamRepository;
import edu.IIT.team_management.service.TeamServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TeamServiceTest {

    private TeamRepository teamRepository;
    private ModelMapper modelMapper;
    private TeamProducer teamProducer;
    private TeamServiceImpl teamService;

    @BeforeEach
    void setUp() {
        teamRepository = mock(TeamRepository.class);
        modelMapper = new ModelMapper();
        teamProducer = mock(TeamProducer.class);

        teamService = new TeamServiceImpl(teamRepository, modelMapper, teamProducer);
    }

    @Test
    void testCreateTeam() {
        // Arrange
        TeamDTO teamDTO = new TeamDTO();
        teamDTO.setTeamName("Alpha Team");
        teamDTO.setAssignerId(101);
        teamDTO.setCollaboratorIds(Arrays.asList(201, 202));

        Team team = modelMapper.map(teamDTO, Team.class);

        when(teamRepository.save(any(Team.class))).thenReturn(team);

        // Act
        String result = teamService.createTeam(teamDTO);

        // Assert
        assertEquals("Team created successfully", result);
        verify(teamRepository, times(1)).save(any(Team.class));
        verify(teamProducer, times(1)).sendCreatedTeamMessage("Alpha Team", 101, Arrays.asList(201, 202));
    }

    @Test
    void testGetTeamById() {
        Team team = new Team();
        team.setTeamId(1);
        team.setTeamName("DevOps");
        Optional<Team> optionalTeam = Optional.of(team);

        when(teamRepository.findById(1)).thenReturn(optionalTeam);

        TeamDTO result = teamService.getTeamById(1);

        assertEquals("DevOps", result.getTeamName());
    }

    @Test
    void testUpdateTeamSuccess() {
        TeamDTO teamDTO = new TeamDTO();
        teamDTO.setTeamId(1);
        teamDTO.setCollaboratorIds(Arrays.asList(1, 2));

        Team existingTeam = new Team();
        existingTeam.setTeamId(1);
        existingTeam.setTeamName("Original Team");
        existingTeam.setCollaboratorIds(Arrays.asList(2, 3));

        when(teamRepository.findById(1)).thenReturn(Optional.of(existingTeam));

        String result = teamService.updateTeam(teamDTO);

        assertEquals("Team updated successfully", result);
        verify(teamRepository, times(1)).save(any(Team.class));
        verify(teamProducer).sendUpdateTeamMessage(anyString(), anyInt(), eq("New"), anyList());
        verify(teamProducer).sendUpdateTeamMessage(anyString(), anyInt(), eq("Removed"), anyList());
        verify(teamProducer).sendUpdateTeamMessage(anyString(), anyInt(), eq("Existing"), anyList());
    }

    @Test
    void testUpdateTeamNotFound() {
        TeamDTO teamDTO = new TeamDTO();
        teamDTO.setTeamId(999);

        when(teamRepository.findById(999)).thenReturn(Optional.empty());

        String result = teamService.updateTeam(teamDTO);

        assertEquals("Team not found", result);
        verify(teamRepository, never()).save(any());
    }

    @Test
    void testDeleteTeam() {
        TeamDTO teamDTO = new TeamDTO();
        teamDTO.setTeamId(1);
        teamDTO.setTeamName("TestTeam");
        teamDTO.setAssignerId(100);
        teamDTO.setCollaboratorIds(Arrays.asList(5, 6));

        when(teamRepository.findById(1)).thenReturn(Optional.of(modelMapper.map(teamDTO, Team.class)));

        teamService.deleteTeam(1);

        verify(teamRepository).deleteById(1);
        verify(teamProducer).sendDeleteTeamMessage(1, "TestTeam", 100, Arrays.asList(5, 6));
    }

    @Test
    void testGetAllTeams() {
        List<Team> teams = Arrays.asList(new Team(), new Team());
        when(teamRepository.findAll()).thenReturn(teams);

        Type listType = new TypeToken<List<TeamDTO>>(){}.getType();
        List<TeamDTO> result = modelMapper.map(teams, listType);

        assertEquals(2, result.size());
    }

    @Test
    void testFilterTeams() {
        Team team1 = new Team();
        team1.setTeamName("Alpha");
        Team team2 = new Team();
        team2.setTeamName("Beta");

        when(teamRepository.findAllById(Arrays.asList(1, 2))).thenReturn(Arrays.asList(team1, team2));

        List<String> result = teamService.filterTeams(Arrays.asList(1, 2));

        assertEquals(Arrays.asList("Alpha", "Beta"), result);
    }
}
