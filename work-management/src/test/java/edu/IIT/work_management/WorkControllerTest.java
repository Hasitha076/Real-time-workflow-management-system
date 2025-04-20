package edu.IIT.work_management;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.IIT.work_management.controller.WorkController;
import edu.IIT.work_management.dto.WorkDTO;
import edu.IIT.work_management.service.WorkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class WorkControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkService workService;

    @InjectMocks
    private WorkController workController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(workController).build();
    }

    @Test
    void testCreateWork() throws Exception {
        WorkDTO workDTO = new WorkDTO();
        workDTO.setWorkName("New Work");

        when(workService.createWork(any(WorkDTO.class))).thenReturn("Work created successfully");

        mockMvc.perform(post("/api/v1/work")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Work created successfully"));

        verify(workService).createWork(any(WorkDTO.class));
    }

    @Test
    void testGetWorkById() throws Exception {
        int workId = 1;
        WorkDTO workDTO = new WorkDTO();
        workDTO.setWorkId(workId);
        workDTO.setWorkName("Test Work");

        when(workService.getWorkById(workId)).thenReturn(workDTO);

        mockMvc.perform(get("/api/work/{id}", workId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workId").value(workId))
                .andExpect(jsonPath("$.workName").value("Test Work"));

        verify(workService).getWorkById(workId);
    }

    @Test
    void testUpdateWork() throws Exception {
        WorkDTO workDTO = new WorkDTO();
        workDTO.setWorkId(1);
        workDTO.setWorkName("Updated Work");

        when(workService.updateWork(any(WorkDTO.class))).thenReturn("Work updated successfully");

        mockMvc.perform(put("/api/work")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Work updated successfully"));

        verify(workService).updateWork(any(WorkDTO.class));
    }

    @Test
    void testDeleteWork() throws Exception {
        int workId = 1;

        doNothing().when(workService).deleteWork(workId);

        mockMvc.perform(delete("/api/work/{id}", workId))
                .andExpect(status().isOk())
                .andExpect(content().string("Work deleted successfully"));

        verify(workService).deleteWork(workId);
    }

    @Test
    void testDeleteByProjectId() throws Exception {
        int projectId = 10;

        doNothing().when(workService).deleteByProjectId(projectId);

        mockMvc.perform(delete("/api/work/project/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(content().string("Works under the project deleted successfully"));

        verify(workService).deleteByProjectId(projectId);
    }

    @Test
    void testGetAllWorks() throws Exception {
        WorkDTO work1 = new WorkDTO();
        work1.setWorkId(1);
        work1.setWorkName("Work One");

        WorkDTO work2 = new WorkDTO();
        work2.setWorkId(2);
        work2.setWorkName("Work Two");

        List<WorkDTO> workList = Arrays.asList(work1, work2);

        when(workService.getAllWorks()).thenReturn(workList);

        mockMvc.perform(get("/api/work"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].workName").value("Work One"))
                .andExpect(jsonPath("$[1].workName").value("Work Two"));

        verify(workService).getAllWorks();
    }

    @Test
    void testUpdateWorkStatus() throws Exception {
        int workId = 1;
        boolean newStatus = true;

        when(workService.updateWorkStatus(workId, newStatus)).thenReturn("Work status updated successfully");

        mockMvc.perform(put("/api/work/status/{workId}?status={status}", workId, newStatus))
                .andExpect(status().isOk())
                .andExpect(content().string("Work status updated successfully"));

        verify(workService).updateWorkStatus(workId, newStatus);
    }
}
