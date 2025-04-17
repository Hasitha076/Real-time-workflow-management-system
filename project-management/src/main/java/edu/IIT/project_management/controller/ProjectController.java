package edu.IIT.project_management.controller;

import edu.IIT.project_management.dto.CollaboratorsRequest;
import edu.IIT.project_management.dto.ProjectDTO;
import edu.IIT.project_management.dto.ProjectUpdateStatus;
import edu.IIT.project_management.producer.ProjectProducer;
import edu.IIT.project_management.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/project")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectProducer projectProducer;

    @MutationMapping
    public String createProject(@Argument("input") ProjectDTO projectDTO) {
        System.out.println(projectDTO);
        return projectService.createProject(projectDTO);
    }

    @MutationMapping
    public String updateProject(@Argument("input") ProjectDTO projectDTO) {
        return projectService.updateProject(projectDTO);
    }

    @MutationMapping
    public String deleteProject(@Argument int id) {
        projectService.deleteProject(id);
        return "Project deleted successfully";
    }

    @QueryMapping
    public ProjectDTO getProject(@Argument int id) {
        return projectService.getProjectById(id);
    }

    @QueryMapping
    public List<ProjectDTO> getAllProjects() {
        List<ProjectDTO> projects = projectService.getAllProjects();
        System.out.println(projects);
        return projects;
    }

    @MutationMapping
    public String updateCollaborators(@Argument int projectId, @Argument CollaboratorsRequest input) {
        projectService.updateCollaborators(projectId, input);
        return "Collaborators updated successfully";
    }

    @QueryMapping
    public List<ProjectDTO> getProjectsByTeamId(@Argument int teamId) {
        return projectService.getProjectsByTeamId(teamId);
    }

    @QueryMapping
    public List<ProjectDTO> getProjectsByCollaboratorId(@Argument int collaboratorId) {
        return projectService.getProjectsByCollaboratorId(collaboratorId);
    }

    @MutationMapping
    public String updateProjectStatus(@Argument int projectId,  @Argument("input") ProjectUpdateStatus projectUpdateStatus) {
        System.out.println(projectUpdateStatus);
        projectService.updateProjectStatus(projectId, projectUpdateStatus);
        return "Project status updated successfully";
    }

}
