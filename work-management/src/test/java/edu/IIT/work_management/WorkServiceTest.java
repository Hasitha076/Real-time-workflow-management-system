package edu.IIT.work_management;

import edu.IIT.work_management.dto.WorkDTO;
import edu.IIT.work_management.model.Work;
import edu.IIT.work_management.producer.WorkProducer;
import edu.IIT.work_management.repository.WorkRepository;
import edu.IIT.work_management.service.WorkServiceImpl;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkServiceTest {

    private WorkServiceImpl workService;

    @Mock private WorkRepository workRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private WorkProducer workProducer;
    @Mock private WebClient userWebClient;
    @Mock private WebClient teamWebClient;

    @Mock private WebClient.RequestHeadersUriSpec userHeadersSpec;
    @Mock private WebClient.RequestHeadersUriSpec teamHeadersSpec;
    @Mock private WebClient.ResponseSpec userResponseSpec;
    @Mock private WebClient.ResponseSpec teamResponseSpec;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workService = new WorkServiceImpl(workRepository, modelMapper, workProducer, userWebClient, teamWebClient);
    }

    @Test
    void testCreateWork() {
        // Arrange
        WorkDTO workDTO = new WorkDTO();
        workDTO.setWorkName("Test Work");
        workDTO.setAssignerId(1);
        workDTO.setCollaboratorIds(Arrays.asList(2, 3));
        workDTO.setTeamIds(Arrays.asList(4, 5));

        List<String> mockUsers = Arrays.asList("User1", "User2");
        List<String> mockTeams = Arrays.asList("Team1", "Team2");

        // Mock WebClient behavior
        when(userWebClient.get()).thenReturn(userHeadersSpec);
        when(userHeadersSpec.uri(any(Function.class))).thenReturn(userHeadersSpec);
        when(userHeadersSpec.retrieve()).thenReturn(userResponseSpec);
        when(userResponseSpec.bodyToMono(List.class)).thenReturn(Mono.just(mockUsers));

        when(teamWebClient.get()).thenReturn(teamHeadersSpec);
        when(teamHeadersSpec.uri(any(Function.class))).thenReturn(teamHeadersSpec);
        when(teamHeadersSpec.retrieve()).thenReturn(teamResponseSpec);
        when(teamResponseSpec.bodyToMono(List.class)).thenReturn(Mono.just(mockTeams));

        // Mock mapping and saving
        Work mockWork = new Work();
        mockWork.setWorkId(1);
        mockWork.setWorkName("Test Work");
        when(modelMapper.map(any(WorkDTO.class), eq(Work.class))).thenReturn(mockWork);
        when(workRepository.save(any())).thenReturn(mockWork);

        doNothing().when(workProducer).sendCreateWorkMessage(anyString(), anyInt(), anyList());

        // Act
        String result = workService.createWork(workDTO);

        // Assert
        assertEquals("Work created successfully", result);
        assertEquals(Arrays.asList("U", "U", "T", "T"), workDTO.getMemberIcons());

        verify(workRepository).save(any());
        verify(workProducer).sendCreateWorkMessage(eq("Test Work"), eq(1), eq(Arrays.asList(2, 3)));
    }

    @Test
    void testGetWorkById() {
        // Arrange
        int workId = 1;
        Work mockWork = new Work();
        mockWork.setWorkId(workId);
        mockWork.setWorkName("Test Work");

        // Mock repository behavior
        when(workRepository.findById(workId)).thenReturn(Optional.of(mockWork));

        // Mock model mapping
        WorkDTO mockWorkDTO = new WorkDTO();
        mockWorkDTO.setWorkId(workId);
        mockWorkDTO.setWorkName("Test Work");
        when(modelMapper.map(mockWork, WorkDTO.class)).thenReturn(mockWorkDTO);

        // Act
        WorkDTO result = workService.getWorkById(workId);

        // Assert
        assertEquals("Test Work", result.getWorkName());
        assertEquals(workId, result.getWorkId());

        verify(workRepository).findById(workId);
        verify(modelMapper).map(mockWork, WorkDTO.class);
    }

    @Test
    void testUpdateWork() {
        // Arrange
        int workId = 1;
        WorkDTO workDTO = new WorkDTO();
        workDTO.setWorkId(workId);
        workDTO.setWorkName("Updated Work");
        workDTO.setAssignerId(1);
        workDTO.setCollaboratorIds(Arrays.asList(2, 3, 6)); // New collaborators
        workDTO.setTeamIds(Arrays.asList(4, 5));

        List<String> mockUsers = Arrays.asList("User1", "User2", "User3");
        List<String> mockTeams = Arrays.asList("Team1", "Team2");

        // Mock existing work in repository
        Work existingWork = new Work();
        existingWork.setWorkId(workId);
        existingWork.setWorkName("Old Work");
        existingWork.setCollaboratorIds(Arrays.asList(2, 3)); // Old collaborators
        existingWork.setTeamIds(Arrays.asList(4, 5));

        // Mock WebClient behavior
        when(workRepository.findById(workId)).thenReturn(Optional.of(existingWork));

        when(userWebClient.get()).thenReturn(userHeadersSpec);
        when(userHeadersSpec.uri(any(Function.class))).thenReturn(userHeadersSpec);
        when(userHeadersSpec.retrieve()).thenReturn(userResponseSpec);
        when(userResponseSpec.bodyToMono(List.class)).thenReturn(Mono.just(mockUsers));

        when(teamWebClient.get()).thenReturn(teamHeadersSpec);
        when(teamHeadersSpec.uri(any(Function.class))).thenReturn(teamHeadersSpec);
        when(teamHeadersSpec.retrieve()).thenReturn(teamResponseSpec);
        when(teamResponseSpec.bodyToMono(List.class)).thenReturn(Mono.just(mockTeams));

        // Mock model mapping and saving
        Work updatedWork = new Work();
        updatedWork.setWorkId(workId);
        updatedWork.setWorkName("Updated Work");

        // Use lenient stubbing here to allow argument mismatch issues to be ignored
        lenient().when(modelMapper.map(any(WorkDTO.class), eq(Work.class))).thenReturn(updatedWork);

        when(workRepository.save(any())).thenReturn(updatedWork);

        doNothing().when(workProducer).sendUpdateWorkMessage(anyString(), anyInt(), anyString(), anyList());

        // Act
        String result = workService.updateWork(workDTO);

        // Assert
        assertEquals("Work updated successfully", result);
        assertEquals(Arrays.asList("U", "U", "U", "T", "T"), workDTO.getMemberIcons()); // Check initials

        // Verify that repository and producer methods were called correctly
        verify(workRepository).findById(workId);
        verify(workRepository).save(any());
        verify(workProducer).sendUpdateWorkMessage(eq("Updated Work"), eq(1), eq("New"), eq(Arrays.asList(6)));
        verify(workProducer).sendUpdateWorkMessage(eq("Updated Work"), eq(1), eq("Existing"), eq(Arrays.asList(2, 3)));
    }

    @Test
    void testDeleteWork() {
        // Arrange
        int workId = 1;

        Work mockWork = new Work();
        mockWork.setWorkId(workId);
        mockWork.setWorkName("Test Work");
        mockWork.setAssignerId(1);
        mockWork.setCollaboratorIds(Arrays.asList(2, 3));
        mockWork.setProjectId(10);

        WorkDTO mockWorkDTO = new WorkDTO();
        mockWorkDTO.setWorkId(workId);
        mockWorkDTO.setWorkName("Test Work");
        mockWorkDTO.setAssignerId(1);
        mockWorkDTO.setCollaboratorIds(Arrays.asList(2, 3));
        mockWorkDTO.setProjectId(10);

        // Mock repository and mapping behavior
        when(workRepository.findById(workId)).thenReturn(Optional.of(mockWork));
        when(modelMapper.map(mockWork, WorkDTO.class)).thenReturn(mockWorkDTO);

        doNothing().when(workRepository).deleteById(workId);
        doNothing().when(workProducer).sendDeleteWorkMessage(anyInt(), anyString(), anyInt(), anyList());

        // Act
        workService.deleteWork(workId);

        // Assert
        verify(workRepository).deleteById(workId);
        verify(workProducer).sendDeleteWorkMessage(eq(10), eq("Test Work"), eq(1), eq(Arrays.asList(2, 3)));
    }

    @Test
    void testDeleteByProjectId_Success() {
        // Arrange
        int projectId = 10;

        Work work1 = new Work();
        work1.setWorkId(1);
        work1.setWorkName("Work One");
        work1.setAssignerId(100);
        work1.setCollaboratorIds(Arrays.asList(200, 201));
        work1.setProjectId(projectId);

        Work work2 = new Work();
        work2.setWorkId(2);
        work2.setWorkName("Work Two");
        work2.setAssignerId(101);
        work2.setCollaboratorIds(Arrays.asList(202, 203));
        work2.setProjectId(projectId);

        List<Work> works = Arrays.asList(work1, work2);

        when(workRepository.findByProjectId(projectId)).thenReturn(works);
        doNothing().when(workRepository).deleteById(anyInt());
        doNothing().when(workProducer).sendDeleteWorkMessage(anyInt(), anyString(), anyInt(), anyList());

        // Act
        workService.deleteByProjectId(projectId);

        // Assert
        verify(workRepository).findByProjectId(projectId);
        verify(workRepository).deleteById(1);
        verify(workRepository).deleteById(2);

        verify(workProducer).sendDeleteWorkMessage(eq(1), eq("Work One"), eq(100), eq(Arrays.asList(200, 201)));
        verify(workProducer).sendDeleteWorkMessage(eq(2), eq("Work Two"), eq(101), eq(Arrays.asList(202, 203)));
    }

    @Test
    void testDeleteByProjectId_NoWorkFound() {
        // Arrange
        int projectId = 99;
        when(workRepository.findByProjectId(projectId)).thenReturn(List.of());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workService.deleteByProjectId(projectId)
        );

        assertEquals("No work found for project ID: 99", exception.getMessage());
        verify(workRepository).findByProjectId(projectId);
        verifyNoMoreInteractions(workRepository, workProducer);
    }

    @Test
    void testGetAllWorks() {
        // Arrange
        Work work1 = new Work();
        work1.setWorkId(1);
        work1.setWorkName("Work One");

        Work work2 = new Work();
        work2.setWorkId(2);
        work2.setWorkName("Work Two");

        List<Work> mockWorks = Arrays.asList(work1, work2);

        WorkDTO workDTO1 = new WorkDTO();
        workDTO1.setWorkId(1);
        workDTO1.setWorkName("Work One");

        WorkDTO workDTO2 = new WorkDTO();
        workDTO2.setWorkId(2);
        workDTO2.setWorkName("Work Two");

        List<WorkDTO> mockWorkDTOs = Arrays.asList(workDTO1, workDTO2);

        when(workRepository.findAll()).thenReturn(mockWorks);
        when(modelMapper.map(eq(mockWorks), any(Type.class))).thenReturn(mockWorkDTOs);

        // Act
        List<WorkDTO> result = workService.getAllWorks();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Work One", result.get(0).getWorkName());
        assertEquals("Work Two", result.get(1).getWorkName());

        verify(workRepository).findAll();
        verify(modelMapper).map(eq(mockWorks), any(Type.class));
    }

    @Test
    void testUpdateWorkStatus() {
        // Arrange
        int workId = 1;
        boolean newStatus = true;

        WorkDTO workDTO = new WorkDTO();
        workDTO.setWorkId(workId);
        workDTO.setStatus(false); // initial status

        Work mappedWork = new Work();
        mappedWork.setWorkId(workId);
        mappedWork.setStatus(true); // after update

        // Mock getWorkById (called internally)
        when(workRepository.findById(workId)).thenReturn(Optional.of(new Work()));
        when(modelMapper.map(any(Work.class), eq(WorkDTO.class))).thenReturn(workDTO);
        when(modelMapper.map(any(WorkDTO.class), any(Type.class))).thenReturn(mappedWork);
        when(workRepository.save(any(Work.class))).thenReturn(mappedWork);

        // Act
        String result = workService.updateWorkStatus(workId, newStatus);

        // Assert
        assertEquals("Work status updated successfully", result);
        assertEquals(true, workDTO.isStatus());

        verify(workRepository).findById(workId);
        verify(workRepository).save(any(Work.class));
    }






}
