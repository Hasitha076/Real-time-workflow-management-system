package edu.IIT.team_management.controller;

import edu.IIT.team_management.dto.TeamDTO;
import edu.IIT.team_management.producer.TeamProducer;
import edu.IIT.team_management.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/team")
@RequiredArgsConstructor
@CrossOrigin
public class TeamController {

    private final TeamService teamService;
    private final TeamProducer teamProducer;

    @PostMapping("/createTeam")
    public String addTeam(@RequestBody TeamDTO teamDTO) {
        return teamService.createTeam(teamDTO);
    }

    @PutMapping("/updateTeam")
    public String updateTeam(@RequestBody TeamDTO teamDTO) {
        return teamService.updateTeam(teamDTO);
    }

    @DeleteMapping("/deleteTeam/{id}")
    public String deleteTeam(@PathVariable int id) {
        teamService.deleteTeam(id);
        return "Team deleted successfully";
    }

    @GetMapping("/getTeam/{id}")
    public TeamDTO getTeam(@PathVariable int id) {
        return teamService.getTeamById(id);
    }

    @GetMapping("/getAllTeams")
    public List<TeamDTO> getAllTeams() {
        return teamService.getAllTeams();
    }

    @GetMapping("/filterTeams")
    public List<String> filterTeams(@RequestParam List<Integer> ids) {
        return teamService.filterTeams(ids);
    }

}
