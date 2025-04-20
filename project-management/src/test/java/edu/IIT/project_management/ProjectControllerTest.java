package edu.IIT.project_management;

import edu.IIT.project_management.controller.ProjectController;
import edu.IIT.project_management.dto.CollaboratorsRequest;
import edu.IIT.project_management.dto.ProjectDTO;
import edu.IIT.project_management.dto.ProjectUpdateStatus;
import edu.IIT.project_management.producer.ProjectProducer;
import edu.IIT.project_management.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProjectControllerTest {

    private ProjectService projectService;
    private ProjectProducer projectProducer;
    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        projectService = mock(ProjectService.class);
        projectProducer = mock(ProjectProducer.class);
        projectController = new ProjectController(projectService, projectProducer);
    }

    @Test
    void testCreateProject() {
        ProjectDTO dto = new ProjectDTO();
        when(projectService.createProject(dto)).thenReturn("Project created successfully");

        String result = projectController.createProject(dto);
        assertEquals("Project created successfully", result);
        verify(projectService).createProject(dto);
    }

    @Test
    void testUpdateProject() {
        ProjectDTO dto = new ProjectDTO();
        when(projectService.updateProject(dto)).thenReturn("Project updated successfully");

        String result = projectController.updateProject(dto);
        assertEquals("Project updated successfully", result);
        verify(projectService).updateProject(dto);
    }

    @Test
    void testDeleteProject() {
        doNothing().when(projectService).deleteProject(1);
        String result = projectController.deleteProject(1);
        assertEquals("Project deleted successfully", result);
        verify(projectService).deleteProject(1);
    }

    @Test
    void testGetProject() {
        ProjectDTO dto = new ProjectDTO();
        when(projectService.getProjectById(1)).thenReturn(dto);

        ProjectDTO result = projectController.getProject(1);
        assertEquals(dto, result);
        verify(projectService).getProjectById(1);
    }

    @Test
    void testGetAllProjects() {
        List<ProjectDTO> projects = Arrays.asList(new ProjectDTO(), new ProjectDTO());
        when(projectService.getAllProjects()).thenReturn(projects);

        List<ProjectDTO> result = projectController.getAllProjects();
        assertEquals(projects.size(), result.size());
        verify(projectService).getAllProjects();
    }

    @Test
    void testUpdateCollaborators() {
        CollaboratorsRequest input = new CollaboratorsRequest();
        doNothing().when(projectService).updateCollaborators(1, input);

        String result = projectController.updateCollaborators(1, input);
        assertEquals("Collaborators updated successfully", result);
        verify(projectService).updateCollaborators(1, input);
    }

    @Test
    void testGetProjectsByTeamId() {
        List<ProjectDTO> projects = Arrays.asList(new ProjectDTO());
        when(projectService.getProjectsByTeamId(101)).thenReturn(projects);

        List<ProjectDTO> result = projectController.getProjectsByTeamId(101);
        assertEquals(projects, result);
        verify(projectService).getProjectsByTeamId(101);
    }

    @Test
    void testGetProjectsByCollaboratorId() {
        List<ProjectDTO> projects = Arrays.asList(new ProjectDTO());
        when(projectService.getProjectsByCollaboratorId(2)).thenReturn(projects);

        List<ProjectDTO> result = projectController.getProjectsByCollaboratorId(2);
        assertEquals(projects, result);
        verify(projectService).getProjectsByCollaboratorId(2);
    }

    @Test
    void testUpdateProjectStatus() {
        ProjectUpdateStatus status = new ProjectUpdateStatus();
        doNothing().when(projectService).updateProjectStatus(1, status);

        String result = projectController.updateProjectStatus(1, status);
        assertEquals("Project status updated successfully", result);
        verify(projectService).updateProjectStatus(1, status);
    }
}
