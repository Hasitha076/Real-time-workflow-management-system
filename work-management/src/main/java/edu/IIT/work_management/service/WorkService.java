package edu.IIT.work_management.service;

import edu.IIT.work_management.dto.WorkDTO;

import java.util.List;

public interface WorkService {

    public String createWork(WorkDTO workDTO);
    public WorkDTO getWorkById(int id);
    public String updateWork(WorkDTO workDTO);
    public void deleteWork(int id);
    public List<WorkDTO> getAllWorks();
    public void deleteByProjectId(int projectId);
}
