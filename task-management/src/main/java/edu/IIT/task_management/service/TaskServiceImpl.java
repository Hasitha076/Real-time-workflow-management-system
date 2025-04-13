package edu.IIT.task_management.service;

//import edu.IIT.project_management.dto.ProjectDTO;
import edu.IIT.task_management.dto.*;
//import edu.IIT.task_management.dto.TaskTemplateDTO;
import edu.IIT.task_management.model.*;
import edu.IIT.task_management.producer.TaskProducer;
import edu.IIT.task_management.repository.*;
//import edu.IIT.task_management.repository.TaskTemplateRepository;
//import edu.IIT.work_management.dto.WorkDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TemplateRepository templateRepository;
    private final CollaboratorsBlockRepository collaboratorsBlockRepository;
    private final RuleRepository ruleRepository;
    private final PublishFlowRepository publishFlowRepository;
    private final ModelMapper modelMapper;
    private final TaskProducer taskProducer;
    private final WebClient userWebClient;
    private final WebClient teamWebClient;
    private final WebClient workWebClient;

    @Override
    public String createTask(TaskDTO taskDTO) {

        List<String> users = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUserNames") // Ensure path matches the controller
                        .queryParam("ids", taskDTO.getCollaboratorIds()) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        List<String> teams = teamWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterTeams") // Ensure path matches the controller
                        .queryParam("ids", taskDTO.getTeamIds()) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        System.out.println("Users: " + users);
        System.out.println("Teams: " + teams);


        List<String> userInitials = users.stream()
                .map(name -> name.substring(0, 1).toUpperCase())
                .collect(Collectors.toList());

        List<String> teamsInitials = teams.stream()
                .map(name -> name.substring(0, 1).toUpperCase())
                .collect(Collectors.toList());

        // Combine initials
        List<String> memberIcons = new ArrayList<>();
        memberIcons.addAll(userInitials);
        memberIcons.addAll(teamsInitials);

        // Set the member icons in the project
        taskDTO.setMemberIcons(memberIcons);


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

        System.out.println("Task updated: " + taskDTO);
        Optional<Task> task = (taskRepository.findById(taskDTO.getTaskId()));
        if (task.isEmpty()) {
            return "Task not found";
        }

        if(taskDTO.getComments() != null) {
            List<String> comment = modelMapper.map(task.get().getComments(), new TypeToken<ArrayList<String>>(){}.getType());
            comment.add(taskDTO.getComments().get(0));

            taskDTO.setComments(comment);
        } else {
            taskDTO.setComments(task.get().getComments());
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

    @Transactional
    @Override
    public void deleteByProjectId(int projectId) {
        // Fetch all tasks that match the project ID
        List<Task> tasks = taskRepository.findByProjectId(projectId);

        if (tasks.isEmpty()) {
            throw new ResourceNotFoundException("No tasks found for project ID: " + projectId);
        }

        tasks.forEach(task -> {
            // Delete each task
            taskRepository.deleteById(task.getTaskId());

            // Send notification for deleted task
            taskProducer.sendDeleteTaskMessage(task.getTaskName(), task.getAssignerId(), task.getCollaboratorIds());
        });
    }

    @Transactional
    @Override
    public void deleteByWorkId(int workId) {
        // Fetch all tasks that match the project ID
        List<Task> tasks = taskRepository.findByWorkId(workId);

        if (tasks.isEmpty()) {
            throw new ResourceNotFoundException("No tasks found for work ID: " + workId);
        }

        tasks.forEach(task -> {
            // Delete each task
            taskRepository.deleteById(task.getTaskId());

            // Send notification for deleted task
            taskProducer.sendDeleteTaskMessage(task.getTaskName(), task.getAssignerId(), task.getCollaboratorIds());
        });
    }

    @Override
    public List<TaskDTO> getAllTasks() {
        return modelMapper.map(taskRepository.findAll(), new TypeToken<List<TaskDTO>>(){}.getType());
    }

    @Override
    public List<TaskDTO> getTasksByProjectId(int projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);

        if (tasks.isEmpty()) {
            return Collections.emptyList(); // Return an empty list instead of mapping null
        }

        return modelMapper.map(tasks, new TypeToken<List<TaskDTO>>(){}.getType());
    }


    @Override
    public List<TaskDTO> getTasksByWorkId(int workId) {
        System.out.println("Called: "+ workId);
        // Fetch all tasks that match the project ID
        List<Task> tasks = taskRepository.findByWorkId(workId);
        System.out.println("Called: "+ workId);
        System.out.println("Tasks: "+ tasks);
        if (tasks.isEmpty()) {
            tasks = new ArrayList<>();
            return modelMapper.map(tasks, new TypeToken<List<TaskDTO>>(){}.getType());
        }

        return modelMapper.map(tasks, new TypeToken<List<TaskDTO>>(){}.getType());
    }

    public void changeTaskStatus(int taskId) {
        // Fetch task properly
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Fetch works using WebClient with generic Map response
        List<Map<String, Object>> work = workWebClient.get()
                .uri("/getWorksByProjectId/{id}", task.getProjectId()) // Correct URI usage
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();

        System.out.println("work ======> " + work);

        // Extract workIds assuming each work object has a key named "workId"
        List<Integer> workIds = work.stream()
                .map(workMap -> (Integer) workMap.get("workId")) // Extract workId from map
                .collect(Collectors.toList());

        int taskIndex = workIds.indexOf(task.getWorkId());
        System.out.println("Task index in workIds: " + taskIndex);

        // Update the task status
        if (workIds.size() > taskIndex + 1) {
            task.setWorkId(workIds.get(taskIndex + 1));
        } else {
            task.setStatus(true);
        }

        // Save the updated task
        taskRepository.save(task);
    }


    //    Task Template methods
    @Override
    public String createTaskTemplate(TemplateDTO templateDTO) {
        templateRepository.save(modelMapper.map(templateDTO, Template.class));
        return "Task template created successfully";
    }

    @Override
    public TemplateDTO getTaskTemplateById(int id) {
        return modelMapper.map(templateRepository.findById(id), TemplateDTO.class);
    }

    @Override
    public List<TemplateDTO> getTaskTemplatesByProjectId(int projectId) {
        return modelMapper.map(templateRepository.findByProjectId(projectId), new TypeToken<List<TemplateDTO>>(){}.getType());
    }

    @Override
    public String updateTaskTemplate(TemplateDTO taskTemplateDTO) {
        templateRepository.save(modelMapper.map(taskTemplateDTO, new TypeToken<Template>(){}.getType()));
        return "Task template updated successfully";
    }

    @Override
    public void deleteTaskTemplate(int id) {
        templateRepository.deleteById(id);
    }

    @Override
    public List<TemplateDTO> getAllTaskTemplates() {
        return modelMapper.map(templateRepository.findAll(), new TypeToken<List<TemplateDTO>>(){}.getType());
    }


    @Override
    public void createCollaboratorsBlock(CollaboratorsBlockDTO collaboratorsBlockDTO) {
        collaboratorsBlockRepository.save(modelMapper.map(collaboratorsBlockDTO, CollaboratorsBlock.class));
    }

    @Override
    public String updateCollaboratorsBlock(CollaboratorsBlockDTO collaboratorsBlockDTO) {
        System.out.println("Updated CollaboratorsBlockDTO: " + collaboratorsBlockDTO);
        collaboratorsBlockRepository.save(modelMapper.map(collaboratorsBlockDTO, new TypeToken<CollaboratorsBlock>(){}.getType()));
        return "Collaborators block updated successfully";
    }

    @Override
    public CollaboratorsBlockDTO getCollaboratorsBlockByWorkId(int workId) {
        CollaboratorsBlock collaboratorsBlock = collaboratorsBlockRepository.findByWorkId(workId);

        if (collaboratorsBlock == null) {
            return null; // Return null instead of wrong object
        }

        // Ensure correct conversion from Model -> DTO
        return modelMapper.map(collaboratorsBlock, CollaboratorsBlockDTO.class);
    }

    @Override
    public String createRule(RuleDTO ruleDTO) {
        ruleRepository.save(modelMapper.map(ruleDTO, Rule.class));
        return "Rule created successfully";
    }

    @Override
    public RuleDTO getRuleById(int id) {
        return modelMapper.map(ruleRepository.findById(id), RuleDTO.class);
    }

    @Override
    public String updateRule(RuleDTO ruleDTO) {
        ruleRepository.save(modelMapper.map(ruleDTO, new TypeToken<Rule>(){}.getType()));
        return "Rule updated successfully";
    }

    @Override
    public void deleteRule(int id) {
        ruleRepository.findById(id);
    }

    @Override
    public List<RuleDTO> getAllRules() {
        return modelMapper.map(ruleRepository.findAll(), new TypeToken<List<RuleDTO>>(){}.getType());
    }

    @Override
    public List<RuleDTO> getRulesByProjectId(int projectId) {
        return modelMapper.map(ruleRepository.findRulesByProjectId(projectId), new TypeToken<List<RuleDTO>>(){}.getType());
    }

    @Override
    public String createPublishFlow(PublishFlowDTO publishFlowDTO) {
        try {
            List<PublishFlow> existingPublishFlows = publishFlowRepository.findAll();

            // Check if a record with the same projectId exists
            Optional<PublishFlow> matchedFlow = existingPublishFlows.stream()
                    .filter(flow -> flow.getProjectId() == publishFlowDTO.getProjectId())
                    .findFirst();

            if (matchedFlow.isPresent()) {
                // Update the existing record
                PublishFlow flowToUpdate = matchedFlow.get();
                flowToUpdate.setRuleName(publishFlowDTO.getRuleName());
                flowToUpdate.setTriggers(publishFlowDTO.getTriggers());
                flowToUpdate.setActions(publishFlowDTO.getActions());
                flowToUpdate.setUpdatedAt(LocalDateTime.now());

                publishFlowRepository.save(flowToUpdate);
            } else {
                // Save as a new record
                PublishFlow newFlow = modelMapper.map(publishFlowDTO, PublishFlow.class);
                newFlow.setCreatedAt(LocalDateTime.now());
                newFlow.setUpdatedAt(LocalDateTime.now());
                publishFlowRepository.save(newFlow);
            }

            return "Publish flow created or updated successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error while creating or updating publish flow";
        }
    }



    @Override
    public PublishFlowDTO getPublishFlowById(int id) {
        return modelMapper.map(publishFlowRepository.findById(id), PublishFlowDTO.class);
    }

    @Override
    public String updatePublishFlow(PublishFlowDTO publishFlowDTO) {
        publishFlowRepository.save(modelMapper.map(publishFlowDTO, new TypeToken<PublishFlow>(){}.getType()));
        return "Publish flow updated successfully";
    }

    @Override
    public void deletePublishFlow(int id) {
        publishFlowRepository.deleteById(id);
    }

    @Override
    public PublishFlowDTO findPublishFlowByProjectId(int projectId) {
        PublishFlow publishFlow = publishFlowRepository.findPublishFlowByProjectId(projectId);
        if (publishFlow == null) {
            return null; // Return null instead of wrong object
        }
        return modelMapper.map(publishFlow, PublishFlowDTO.class);
    }

}
