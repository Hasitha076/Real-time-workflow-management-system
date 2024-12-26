package edu.IIT.project_management.service;

import edu.IIT.project_management.dto.ProjectDTO;

import java.util.List;

public interface ProjectService {

    public String createProject(ProjectDTO projectDTO);
    public ProjectDTO getProjectById(int id);
    public String updateProject(ProjectDTO projectDTO);
    public void deleteProject(int id);
    public List<ProjectDTO> getAllProjects();
}
