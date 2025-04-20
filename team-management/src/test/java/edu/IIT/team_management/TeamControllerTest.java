package edu.IIT.team_management;

import edu.IIT.team_management.controller.TeamController;
import edu.IIT.team_management.dto.TeamDTO;
import edu.IIT.team_management.producer.TeamProducer;
import edu.IIT.team_management.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

public class TeamControllerTest {

    @Mock
    private TeamService teamService;

    @Mock
    private TeamProducer teamProducer;

    private TeamController teamController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        teamController = new TeamController(teamService, teamProducer);
        mockMvc = MockMvcBuilders.standaloneSetup(teamController).build();
    }

    @Test
    void testAddTeam() throws Exception {
        TeamDTO teamDTO = new TeamDTO();
        teamDTO.setTeamName("Test Team");
        teamDTO.setAssignerId(1);
        teamDTO.setCollaboratorIds(Arrays.asList(2, 3));

        // Mock the service call
        when(teamService.createTeam(teamDTO)).thenReturn("Team created successfully");

        // Perform the POST request
        mockMvc.perform(post("/createTeam")
                        .contentType("application/json")
                        .content("{\"teamName\": \"Test Team\", \"assignerId\": 1, \"collaboratorIds\": [2, 3]}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Team created successfully"));

        // Verify interactions
        verify(teamService).createTeam(teamDTO);
    }

    @Test
    void testUpdateTeam() throws Exception {
        TeamDTO teamDTO = new TeamDTO();
        teamDTO.setTeamId(1);
        teamDTO.setTeamName("Updated Team");
        teamDTO.setAssignerId(1);
        teamDTO.setCollaboratorIds(Arrays.asList(2, 3));

        // Mock the service call
        when(teamService.updateTeam(teamDTO)).thenReturn("Team updated successfully");

        // Perform the PUT request
        mockMvc.perform(put("/updateTeam")
                        .contentType("application/json")
                        .content("{\"teamId\": 1, \"teamName\": \"Updated Team\", \"assignerId\": 1, \"collaboratorIds\": [2, 3]}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Team updated successfully"));

        // Verify interactions
        verify(teamService).updateTeam(teamDTO);
    }

    @Test
    void testDeleteTeam() throws Exception {
        int teamId = 1;

        // Perform the DELETE request
        mockMvc.perform(delete("/deleteTeam/{id}", teamId))
                .andExpect(status().isOk())
                .andExpect(content().string("Team deleted successfully"));

        // Verify interactions
        verify(teamService).deleteTeam(teamId);
    }

    @Test
    void testGetTeam() throws Exception {
        int teamId = 1;
        TeamDTO teamDTO = new TeamDTO();
        teamDTO.setTeamId(teamId);
        teamDTO.setTeamName("Test Team");
        teamDTO.setAssignerId(1);
        teamDTO.setCollaboratorIds(Arrays.asList(2, 3));

        // Mock the service call
        when(teamService.getTeamById(teamId)).thenReturn(teamDTO);

        // Perform the GET request
        mockMvc.perform(get("/getTeam/{id}", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(teamId))
                .andExpect(jsonPath("$.teamName").value("Test Team"))
                .andExpect(jsonPath("$.assignerId").value(1))
                .andExpect(jsonPath("$.collaboratorIds[0]").value(2))
                .andExpect(jsonPath("$.collaboratorIds[1]").value(3));

        // Verify interactions
        verify(teamService).getTeamById(teamId);
    }

    @Test
    void testGetAllTeams() throws Exception {
        TeamDTO teamDTO1 = new TeamDTO();
        teamDTO1.setTeamId(1);
        teamDTO1.setTeamName("Test Team 1");

        TeamDTO teamDTO2 = new TeamDTO();
        teamDTO2.setTeamId(2);
        teamDTO2.setTeamName("Test Team 2");

        List<TeamDTO> teamDTOList = Arrays.asList(teamDTO1, teamDTO2);

        // Mock the service call
        when(teamService.getAllTeams()).thenReturn(teamDTOList);

        // Perform the GET request
        mockMvc.perform(get("/getAllTeams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].teamName").value("Test Team 1"))
                .andExpect(jsonPath("$[1].teamName").value("Test Team 2"));

        // Verify interactions
        verify(teamService).getAllTeams();
    }

    @Test
    void testFilterTeams() throws Exception {
        List<String> teamNames = Arrays.asList("Test Team 1", "Test Team 2");

        // Mock the service call
        when(teamService.filterTeams(Arrays.asList(1, 2))).thenReturn(teamNames);

        // Perform the GET request with query parameters
        mockMvc.perform(get("/filterTeams")
                        .param("ids", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("Test Team 1"))
                .andExpect(jsonPath("$[1]").value("Test Team 2"));

        // Verify interactions
        verify(teamService).filterTeams(Arrays.asList(1, 2));
    }
}