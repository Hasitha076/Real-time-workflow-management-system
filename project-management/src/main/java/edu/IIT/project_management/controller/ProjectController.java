package edu.IIT.project_management.controller;

import edu.IIT.project_management.dto.CollaboratorsRequest;
import edu.IIT.project_management.dto.ProjectDTO;
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
@CrossOrigin
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectProducer projectProducer;

    @MutationMapping
    public String createProject(@Argument("input") ProjectDTO projectDTO) {
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
        return projectService.getAllProjects();
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

}
