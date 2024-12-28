package edu.IIT.task_management.service;

import edu.IIT.project_management.dto.ProjectDTO;
import edu.IIT.task_management.dto.TaskDTO;
import edu.IIT.task_management.model.Task;
import edu.IIT.task_management.producer.TaskProducer;
import edu.IIT.task_management.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;
    private final TaskProducer taskProducer;

    @Override
    public String createTask(TaskDTO taskDTO) {
        taskRepository.save(modelMapper.map(taskDTO, Task.class));
        taskProducer.sendCreateTaskMessage(taskDTO.getTaskName(), taskDTO.getAssignerId(), taskDTO.getCollaboratorIds());
        return "Task created successfully";
    }

    @Override
    public TaskDTO getTaskById(int id) {
        return modelMapper.map(taskRepository.findById(id), TaskDTO.class);
    }

    @Override
    public String updateTask(TaskDTO taskDTO) {
        Optional<Task> task = (taskRepository.findById(taskDTO.getTaskId()));
        if (task.isEmpty()) {
            return "Task not found";
        }

        // Retain the createdAt value from the old task
        taskDTO.setCreatedAt(task.get().getCreatedAt());
        List<Integer> oldCollaboratorIds = task.get().getCollaboratorIds(); // Get existing collaborators

        // Identify new collaborators: present in new project but not in old task
        List<Integer> newCollaborators = taskDTO.getCollaboratorIds().stream()
                .filter(collaboratorId -> !oldCollaboratorIds.contains(collaboratorId))
                .collect(Collectors.toList());

        // Identify removed collaborators: present in old project but not in new task
        List<Integer> removedCollaborators = oldCollaboratorIds.stream()
                .filter(collaboratorId -> !taskDTO.getCollaboratorIds().contains(collaboratorId))
                .collect(Collectors.toList());

        // Identify unchanged collaborators: present in both old and new task
        List<Integer> unchangedCollaborators = oldCollaboratorIds.stream()
                .filter(taskDTO.getCollaboratorIds()::contains)
                .collect(Collectors.toList());

        taskDTO.setCreatedAt(task.get().getCreatedAt());
        taskRepository.save(modelMapper.map(taskDTO, new TypeToken<Task>(){}.getType()));

        // Handle sending collaborator changes to the producer or further actions
        if (!newCollaborators.isEmpty()) {
            // Send newly added collaborators
            taskProducer.sendUpdateTaskMessage(taskDTO.getTaskName(), taskDTO.getAssignerId(),"New", newCollaborators);
        }

        if (!removedCollaborators.isEmpty()) {
            // Send removed collaborators
            taskProducer.sendUpdateTaskMessage(taskDTO.getTaskName(), taskDTO.getAssignerId(),"Removed", removedCollaborators);
        }

        // Optionally, you can also send unchanged collaborators if needed
        if (!unchangedCollaborators.isEmpty()) {
            // Handle unchanged collaborators if necessary
            taskProducer.sendUpdateTaskMessage(taskDTO.getTaskName(), taskDTO.getAssignerId(),"Existing", unchangedCollaborators);
        }

        return "Task updated successfully";
    }

    @Override
    public void deleteTask(int id) {
        TaskDTO task = getTaskById(id);
        taskRepository.deleteById(id);

        taskProducer.sendDeleteTaskMessage(task.getTaskName(), task.getAssignerId(), task.getCollaboratorIds());
    }

    @Override
    public void deleteByProjectId(int projectId) {
        // Fetch all tasks that match the project ID
        List<Task> tasks = taskRepository.findByProjectId(projectId);

        tasks.forEach(task -> {
            taskRepository.deleteById(task.getTaskId());
            taskProducer.sendDeleteTaskMessage(task.getTaskName(), task.getAssignerId(), task.getCollaboratorIds());
        });

    }


    @Override
    public List<TaskDTO> getAllTasks() {
        return modelMapper.map(taskRepository.findAll(), new TypeToken<List<TaskDTO>>(){}.getType());
    }

}
