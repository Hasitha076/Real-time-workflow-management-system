package edu.IIT.project_management.service;

import edu.IIT.project_management.dto.CollaboratorsRequest;
import edu.IIT.project_management.dto.ProjectDTO;
import edu.IIT.project_management.dto.ProjectUpdateStatus;

import java.util.List;

public interface ProjectService {

    public String createProject(ProjectDTO projectDTO);
    public ProjectDTO getProjectById(int id);
    public String updateProject(ProjectDTO projectDTO);
    public void deleteProject(int id);
    public List<ProjectDTO> getAllProjects();
    public void updateCollaborators(int projectId, CollaboratorsRequest collaboratorsRequest);
    public List<ProjectDTO> getProjectsByTeamId(int teamId);
    public List<ProjectDTO> getProjectsByCollaboratorId(int collaboratorId);
    public void updateProjectStatus(int projectId, ProjectUpdateStatus projectUpdateStatus);
}
