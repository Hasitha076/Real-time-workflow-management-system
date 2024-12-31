package edu.IIT.team_management.service;

import edu.IIT.team_management.dto.TeamDTO;

import java.util.List;

public interface TeamService {

    public String createTeam(TeamDTO teamDTO);
    public TeamDTO getTeamById(int id);
    public String updateTeam(TeamDTO teamDTO);
    public void deleteTeam(int id);
    public List<TeamDTO> getAllTeams();
    public List<String> filterTeams(List<Integer> teamIds);
}
