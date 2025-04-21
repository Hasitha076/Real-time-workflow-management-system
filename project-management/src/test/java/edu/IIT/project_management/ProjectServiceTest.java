package edu.IIT.project_management;

import edu.IIT.project_management.dto.ProjectDTO;
import edu.IIT.project_management.dto.ProjectStatus;
import edu.IIT.project_management.dto.ProjectUpdateStatus;
import edu.IIT.project_management.model.Project;
import edu.IIT.project_management.producer.ProjectProducer;
import edu.IIT.project_management.repository.ProjectRepository;
import edu.IIT.project_management.service.ProjectServiceImpl;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ProjectProducer projectProducer;

    @Mock
    private WebClient userWebClient;

    @Mock
    private WebClient teamWebClient;

    private ProjectServiceImpl projectService;

    @Mock
    private ProjectDTO projectDTO;

    private Project project;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize the service with the mocks
        projectService = new ProjectServiceImpl(
                projectRepository,
                modelMapper,
                projectProducer,
                userWebClient,
                teamWebClient
        );

        // Initialize test data for projectDTO and project
        projectDTO = new ProjectDTO();
        projectDTO.setProjectId(1);
        projectDTO.setProjectName("Test Project");
        projectDTO.setAssignerId(1);
        projectDTO.setCollaboratorIds(Arrays.asList(2, 3));
        projectDTO.setTeamIds(Arrays.asList(101, 102));

        project = new Project();
        project.setProjectId(1);
        project.setProjectName("Test Project");
        project.setAssignerId(1);
        project.setCollaboratorIds(Arrays.asList(2, 3));
        project.setTeamIds(Arrays.asList(101, 102));
    }

    @Test
    void testCreateProject() {
        // Arrange
        when(modelMapper.map(projectDTO, Project.class)).thenReturn(project);
        when(projectRepository.save(project)).thenReturn(project);

        // Act
        String result = projectService.createProject(projectDTO);

        // Assert
        assertEquals("Project created successfully", result);
        verify(modelMapper).map(projectDTO, Project.class);
        verify(projectRepository).save(project);
        verify(projectProducer).sendCreatedProjectMessage("Test Project", 1, Arrays.asList(2, 3));
    }

    @Test
    void testUpdateProject() {
        // Arrange: Existing project in repository
        Project existingProject = new Project();
        existingProject.setProjectId(1);
        existingProject.setProjectName("Old Project");
        existingProject.setAssignerId(1);
        existingProject.setCollaboratorIds(Arrays.asList(5, 6));
        existingProject.setTeamIds(Arrays.asList(100, 101));

        // New data to update
        ProjectDTO updatedDTO = new ProjectDTO();
        updatedDTO.setProjectId(1);
        updatedDTO.setProjectName("Updated Project");
        updatedDTO.setAssignerId(1);
        updatedDTO.setCollaboratorIds(Arrays.asList(10, 11));
        updatedDTO.setTeamIds(Arrays.asList(200, 201));
        updatedDTO.setStatus(ProjectStatus.PENDING);

        // Mock repository behavior
        when(projectRepository.findById(1)).thenReturn(Optional.of(existingProject));
        when(projectRepository.save(any(Project.class))).thenReturn(existingProject);

        // Mock WebClient for user initials
        WebClient.RequestHeadersUriSpec uriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);

        when(userWebClient.get()).thenReturn(uriSpecMock);
        when(uriSpecMock.uri(anyString())).thenReturn(headersSpecMock);
        when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(String.class)).thenReturn(Mono.just("U1, U2")); // mock user initials

        // Mock WebClient for team initials if applicable
        WebClient.RequestHeadersUriSpec teamUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec teamHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec teamResponseSpecMock = mock(WebClient.ResponseSpec.class);

        when(teamWebClient.get()).thenReturn(teamUriSpecMock);
        when(teamUriSpecMock.uri(anyString())).thenReturn(teamHeadersSpecMock);
        when(teamHeadersSpecMock.retrieve()).thenReturn(teamResponseSpecMock);
        when(teamResponseSpecMock.bodyToMono(String.class)).thenReturn(Mono.just("T1, T2")); // mock team initials

        // Act
        String result = projectService.updateProject(updatedDTO);

        // Assert
        assertEquals("Project updated successfully", result);
        assertEquals("Updated Project", existingProject.getProjectName());
        assertEquals(Arrays.asList(10, 11), existingProject.getCollaboratorIds());
        assertEquals(Arrays.asList(200, 201), existingProject.getTeamIds());
        assertEquals(ProjectStatus.COMPLETED, existingProject.getStatus());

        // Verify behavior
        verify(projectRepository).findById(1);
        verify(projectRepository).save(existingProject);
        verify(projectProducer).sendUpdateProjectMessage(eq("Updated Project"), eq(1),
                eq("New"), argThat(list -> list.containsAll(Arrays.asList(10, 11))));
    }



    @Test
    void testGetProjectById() {
        // Arrange
        int projectId = 1;
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setProjectName("Test Project");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(modelMapper.map(project, ProjectDTO.class)).thenReturn(projectDTO);

        // Act
        ProjectDTO result = projectService.getProjectById(projectId);

        // Assert
        assertEquals("Test Project", result.getProjectName());
        verify(projectRepository).findById(projectId);
        verify(modelMapper).map(project, ProjectDTO.class);
    }

    @Test
    void testProjectNotFound() {
        // Arrange: Mock repository to return empty
        when(projectRepository.findById(1)).thenReturn(Optional.empty());

        // Act: Call the method under test
        String result = projectService.updateProject(projectDTO);

        // Assert: Assert the result
        assertEquals("Project not found", result);
    }

    @Test
    void testGetProjectsByCollaboratorId_Found() {
        // Arrange: Create sample data
        int collaboratorId = 1;
        Project project1 = new Project();
        project1.setProjectId(1);
        project1.setCollaboratorIds(Arrays.asList(collaboratorId));

        Project project2 = new Project();
        project2.setProjectId(2);
        project2.setCollaboratorIds(Arrays.asList(collaboratorId));

        List<Project> projects = Arrays.asList(project1, project2);

        // Mock repository to return the projects
        when(projectRepository.findByCollaboratorIds(collaboratorId)).thenReturn(projects);

        // Mock the ModelMapper to return ProjectDTOs
        ProjectDTO projectDTO1 = new ProjectDTO();
        projectDTO1.setProjectId(1);

        ProjectDTO projectDTO2 = new ProjectDTO();
        projectDTO2.setProjectId(2);

        when(modelMapper.map(projects, new TypeToken<List<ProjectDTO>>(){}.getType()))
                .thenReturn(Arrays.asList(projectDTO1, projectDTO2));

        // Act: Call the method under test
        List<ProjectDTO> result = projectService.getProjectsByCollaboratorId(collaboratorId);

        // Assert: Verify result and interactions
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getProjectId());
        assertEquals(2, result.get(1).getProjectId());

        verify(projectRepository).findByCollaboratorIds(collaboratorId);
        verify(modelMapper).map(projects, new TypeToken<List<ProjectDTO>>(){}.getType());
    }

    @Test
    void testGetProjectsByCollaboratorId_NotFound() {
        // Arrange: Set up mock to return an empty list
        int collaboratorId = 1;
        when(projectRepository.findByCollaboratorIds(collaboratorId)).thenReturn(List.of());

        // Act & Assert: Ensure the ResourceNotFoundException is thrown
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            projectService.getProjectsByCollaboratorId(collaboratorId);
        });

        assertEquals("No projects found for user ID: 1", exception.getMessage());
        verify(projectRepository).findByCollaboratorIds(collaboratorId);
    }

    @Test
    void testGetAllProjects_Success() {
        // Arrange
        Project project1 = new Project();
        project1.setProjectId(1);
        project1.setProjectName("Project 1");

        Project project2 = new Project();
        project2.setProjectId(2);
        project2.setProjectName("Project 2");

        List<Project> projectList = Arrays.asList(project1, project2);

        ProjectDTO projectDTO1 = new ProjectDTO();
        projectDTO1.setProjectId(1);
        projectDTO1.setProjectName("Project 1");

        ProjectDTO projectDTO2 = new ProjectDTO();
        projectDTO2.setProjectId(2);
        projectDTO2.setProjectName("Project 2");

        List<ProjectDTO> projectDTOList = Arrays.asList(projectDTO1, projectDTO2);

        // FIX: Create the TypeToken properly
        java.lang.reflect.Type projectListType = new TypeToken<List<ProjectDTO>>() {}.getType();

        when(projectRepository.findAll()).thenReturn(projectList);
        when(modelMapper.map(eq(projectList), eq(projectListType))).thenReturn(projectDTOList);

        // Act
        List<ProjectDTO> result = projectService.getAllProjects();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Project 1", result.get(0).getProjectName());
        assertEquals("Project 2", result.get(1).getProjectName());

        verify(projectRepository, times(1)).findAll();
        verify(modelMapper, times(1)).map(eq(projectList), eq(projectListType));
    }

    @Test
    void testGetProjectsByTeamId_Success() {
        // Arrange
        int teamId = 101;

        Project project1 = new Project();
        project1.setProjectId(1);
        project1.setTeamIds(Arrays.asList(teamId));

        Project project2 = new Project();
        project2.setProjectId(2);
        project2.setTeamIds(Arrays.asList(teamId));

        List<Project> projects = Arrays.asList(project1, project2);

        ProjectDTO projectDTO1 = new ProjectDTO();
        projectDTO1.setProjectId(1);

        ProjectDTO projectDTO2 = new ProjectDTO();
        projectDTO2.setProjectId(2);

        java.lang.reflect.Type projectListType = new TypeToken<List<ProjectDTO>>(){}.getType();

        when(projectRepository.findByTeamIds(teamId)).thenReturn(projects);
        when(modelMapper.map(eq(projects), eq(projectListType)))
                .thenReturn(Arrays.asList(projectDTO1, projectDTO2));

        // Act
        List<ProjectDTO> result = projectService.getProjectsByTeamId(teamId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getProjectId());
        assertEquals(2, result.get(1).getProjectId());

        verify(projectRepository).findByTeamIds(teamId);
        verify(modelMapper).map(eq(projects), eq(projectListType));
    }

    @Test
    void testGetProjectsByTeamId_NotFound() {
        // Arrange
        int teamId = 101;
        when(projectRepository.findByTeamIds(teamId)).thenReturn(Collections.emptyList());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            projectService.getProjectsByTeamId(teamId);
        });

        assertEquals("No projects found for team ID: 101", exception.getMessage());
        verify(projectRepository).findByTeamIds(teamId);
    }

    @Test
    void testUpdateProjectStatus_Success() {
        // Arrange
        int projectId = 1;
        Project project = new Project();
        project.setProjectId(projectId);
        project.setStatus(ProjectStatus.ON_GOING);

        ProjectUpdateStatus updateStatus = new ProjectUpdateStatus();
        updateStatus.setStatus(ProjectStatus.COMPLETED);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // Act
        projectService.updateProjectStatus(projectId, updateStatus);

        // Assert
        assertEquals(ProjectStatus.COMPLETED, project.getStatus());
        verify(projectRepository).findById(projectId);
        verify(projectRepository).save(project);
    }

    @Test
    void testUpdateProjectStatus_ProjectNotFound() {
        // Arrange
        int projectId = 1;
        ProjectUpdateStatus updateStatus = new ProjectUpdateStatus();
        updateStatus.setStatus(ProjectStatus.COMPLETED);

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            projectService.updateProjectStatus(projectId, updateStatus);
        });

        assertEquals("Project not found", exception.getMessage());
        verify(projectRepository).findById(projectId);
        verify(projectRepository, never()).save(any(Project.class));
    }




}
