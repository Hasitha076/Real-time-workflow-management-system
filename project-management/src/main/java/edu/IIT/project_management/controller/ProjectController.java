package edu.IIT.project_management.controller;

import edu.IIT.project_management.dto.CollaboratorsRequest;
import edu.IIT.project_management.dto.ProjectDTO;
import edu.IIT.project_management.producer.ProjectProducer;
import edu.IIT.project_management.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/project")
@RequiredArgsConstructor
@CrossOrigin
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectProducer projectProducer;

    @PostMapping("/createProject")
    public String addProject(@RequestBody ProjectDTO projectDTO) {
        return projectService.createProject(projectDTO);
    }

    @PutMapping("/updateProject")
    public String updateProject(@RequestBody ProjectDTO projectDTO) {
        return projectService.updateProject(projectDTO);
    }

    @DeleteMapping("/deleteProject/{id}")
    public String deleteProject(@PathVariable int id) {
        projectService.deleteProject(id);
        return "Project deleted successfully";
    }

    @GetMapping("/getProject/{id}")
    public ProjectDTO getProject(@PathVariable int id) {
        return projectService.getProjectById(id);
    }

    @GetMapping("/getAllProjects")
    public List<ProjectDTO> getAllProjects() {
        return projectService.getAllProjects();
    }

    @PutMapping("/updateCollaborators/{projectId}")
    public void updateCollaborators(@PathVariable int projectId, @RequestBody CollaboratorsRequest collaboratorsRequest) {
        projectService.updateCollaborators(projectId, collaboratorsRequest);
    }

}
