package edu.IIT.task_management.service;

import edu.IIT.task_management.dto.TaskDTO;

import java.util.List;

public interface TaskService {

    public String createTask(TaskDTO taskDTO);
    public TaskDTO getTaskById(int id);
    public String updateTask(TaskDTO taskDTO);
    public void deleteTask(int id);
    public List<TaskDTO> getAllTasks();
    public void deleteByProjectId(int projectId);
    public void deleteByWorkId(int workId);
    public List<TaskDTO> getTasksByProjectId(int projectId);
    public List<TaskDTO> getTasksByWorkId(int workId);
    public void changeTaskStatus(int taskId);
}
