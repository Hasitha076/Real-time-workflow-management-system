package edu.IIT.task_management.service;

//import edu.IIT.project_management.dto.ProjectDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDate;
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

        List<PublishFlow> publishFlows = publishFlowRepository.findPublishFlowsByProjectId(taskDTO.getProjectId());

        if (publishFlows != null && !publishFlows.isEmpty()) {
            for (PublishFlow publishFlow : publishFlows) {
                if (publishFlow.getStatus().equals("active")) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();

                        List<TriggerDTO> triggers = mapper.readValue(
                                publishFlow.getTriggersJson(),
                                new TypeReference<List<TriggerDTO>>() {}
                        );

                        List<ActionDTO> actions = mapper.readValue(
                                publishFlow.getActionsJson(),
                                new TypeReference<List<ActionDTO>>() {}
                        );

                        if (!triggers.isEmpty() && !actions.isEmpty()) {
                            TriggerDTO firstTrigger = triggers.get(0);
                            ActionDTO firstAction = actions.get(0);

                            String triggerType = (String) firstTrigger.getTriggerDetails().get("triggerType");
                            String actionType = (String) firstAction.getActionDetails().get("actionType");

                            switch (triggerType + ":" + actionType) {
                                case "Task is add from:Set assignee to": {

                                        Map<String, Object> assignee = (Map<String, Object>) firstAction.getActionDetails().get("assignee");
                                        if (assignee != null) {
                                            Integer assigneeId = (Integer) assignee.get("id");
                                            List<Integer> updatedCollaborators = taskDTO.getCollaboratorIds() != null
                                                    ? new ArrayList<>(taskDTO.getCollaboratorIds())
                                                    : new ArrayList<>();
                                            if (!updatedCollaborators.contains(assigneeId)) {
                                                updatedCollaborators.add(assigneeId);
                                                taskDTO.setCollaboratorIds(updatedCollaborators);
                                            }
                                    }
                                    break;
                                }

                                case "Task is add from:Move task to section": {

                                Map<String, Object> ActionMovedSection = (Map<String, Object>) firstAction.getActionDetails().get("ActionMovedSection");
                                if (ActionMovedSection != null) {
                                    Integer workId = (Integer) ActionMovedSection.get("workId");
                                    taskDTO.setWorkId(workId);
                                }

                                    break;
                                }

                                case "Task is add from:Clear assignee": {
                                    taskDTO.setCollaboratorIds(new ArrayList<>());
                                    taskDTO.setTeamIds(new ArrayList<>());
                                    break;
                                }

                                case "Task is add from:Complete task": {
                                    taskDTO.setStatus(true);
                                    break;
                                }

                                case "Task is add from:Incomplete task": {
                                    taskDTO.setStatus(false);
                                    break;
                                }

                                case "Task is add from:Set task title": {
                                    String taskName = (String) firstAction.getActionDetails().get("taskName");
                                    taskDTO.setTaskName(taskName);
                                    break;
                                }

                                case "Task is add from:Set task description": {
                                    String taskDescription = (String) firstAction.getActionDetails().get("taskDescription");
                                    taskDTO.setDescription(taskDescription);
                                    break;
                                }

                                case "Task is add from:Set due date to": {
                                    String dateStr = (String) firstAction.getActionDetails().get("date");
                                    LocalDate date = LocalDate.parse(dateStr);
                                    taskDTO.setDueDate(date);
                                    break;
                                }
                                case "Task is add from:Clear due date": {

                                    taskDTO.setDueDate(null);
                                    break;
                                }
                                case "Task is add from:Create task": {
                                    Map<String, Object> task = (Map<String, Object>) firstAction.getActionDetails().get("task");
                                    String taskName = (String) task.get("name");
                                    String description = (String) task.get("description");
                                    LocalDate dueDate = LocalDate.parse((String) task.get("dueDate"));
                                    List<Integer> collaboratorIds = (List<Integer>) task.get("collaboratorIds");
                                    List<Integer> teamIds = (List<Integer>) task.get("teamIds");
                                    String priority = (String) task.get("priority");

                                    System.out.println("Task: " + task);

                                    Map<String, Object> whichSection = (Map<String, Object>) firstAction.getActionDetails().get("whichSection");
                                    Integer ActionWorkId = (Integer) whichSection.get("workId");

                                    TaskDTO newTaskDTO = new TaskDTO();

                                    newTaskDTO.setTaskName(taskName);
                                    newTaskDTO.setDescription(description);
                                    newTaskDTO.setDueDate(dueDate);
                                    newTaskDTO.setCollaboratorIds(collaboratorIds);
                                    newTaskDTO.setTeamIds(teamIds);
                                    newTaskDTO.setTags(taskDTO.getTags());
                                    newTaskDTO.setPriority(TaskPriorityLevel.valueOf(priority.toUpperCase()));
                                    newTaskDTO.setProjectId(taskDTO.getProjectId());
                                    newTaskDTO.setWorkId(ActionWorkId);
                                    newTaskDTO.setAssignerId(taskDTO.getAssignerId());
                                    newTaskDTO.setStatus(false);

                                    taskRepository.save(modelMapper.map(newTaskDTO, Task.class));
                                    taskProducer.sendCreateTaskMessage(newTaskDTO.getTaskName(), newTaskDTO.getAssignerId(), newTaskDTO.getCollaboratorIds());

                                    break;
                                }

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


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

        Optional<Task> taskOptional = taskRepository.findById(taskDTO.getTaskId());
        if (taskOptional.isEmpty()) {
            return "Task not found";
        }

        Task existingTask = taskOptional.get();

        // Handle comments
        if (taskDTO.getComments() != null) {
            List<String> commentList = new ArrayList<>(existingTask.getComments() != null ? existingTask.getComments() : new ArrayList<>());
            commentList.addAll(taskDTO.getComments());
            taskDTO.setComments(commentList);
        } else {
            taskDTO.setComments(existingTask.getComments());
        }

        // Retain the createdAt value from the existing task
        taskDTO.setCreatedAt(existingTask.getCreatedAt());

        // Handle collaborator comparison
        List<Integer> oldCollaboratorIds = existingTask.getCollaboratorIds() != null ? existingTask.getCollaboratorIds() : new ArrayList<>();
        List<Integer> newCollaboratorIds = taskDTO.getCollaboratorIds() != null ? taskDTO.getCollaboratorIds() : new ArrayList<>();

        List<Integer> newCollaborators = newCollaboratorIds.stream()
                .filter(id -> !oldCollaboratorIds.contains(id))
                .collect(Collectors.toList());

        List<Integer> removedCollaborators = oldCollaboratorIds.stream()
                .filter(id -> !newCollaboratorIds.contains(id))
                .collect(Collectors.toList());

        List<Integer> unchangedCollaborators = oldCollaboratorIds.stream()
                .filter(newCollaboratorIds::contains)
                .collect(Collectors.toList());


        List<PublishFlow> publishFlows = publishFlowRepository.findPublishFlowsByProjectId(taskDTO.getProjectId());

        if (publishFlows != null && !publishFlows.isEmpty()) {
            for (PublishFlow publishFlow : publishFlows) {
                if (publishFlow.getStatus().equals("active")) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();

                        List<TriggerDTO> triggers = mapper.readValue(
                                publishFlow.getTriggersJson(),
                                new TypeReference<List<TriggerDTO>>() {}
                        );

                        List<ActionDTO> actions = mapper.readValue(
                                publishFlow.getActionsJson(),
                                new TypeReference<List<ActionDTO>>() {}
                        );

                        if (!triggers.isEmpty() && !actions.isEmpty()) {
                            TriggerDTO firstTrigger = triggers.get(0);
                            ActionDTO firstAction = actions.get(0);

                            String triggerType = (String) firstTrigger.getTriggerDetails().get("triggerType");
                            String actionType = (String) firstAction.getActionDetails().get("actionType");

                            switch (triggerType + ":" + actionType) {

                                case "Assignee is changed:Move task to section": {

                                    if(!newCollaborators.isEmpty()) {
                                        Map<String, Object> ActionMovedSection = (Map<String, Object>) firstAction.getActionDetails().get("ActionMovedSection");
                                        if (ActionMovedSection != null) {
                                            Integer workId = (Integer) ActionMovedSection.get("workId");
                                            taskDTO.setWorkId(workId);
                                        }
                                    }

                                    break;
                                }

                                case "Assignee is changed:Remove task from the project": {

                                    if(!newCollaborators.isEmpty()) {
                                        taskRepository.deleteById(taskDTO.getTaskId());
                                    }

                                    break;
                                }

                                case "Assignee is changed:Complete task": {

                                    if(!newCollaborators.isEmpty()) {
                                        taskDTO.setStatus(true);
                                    }

                                    break;
                                }

                                case "Assignee is changed:Incomplete task": {

                                    if(!newCollaborators.isEmpty()) {
                                        taskDTO.setStatus(false);
                                    }

                                    break;
                                }

                                case "Assignee is empty:Move task to section": {

                                    if(newCollaboratorIds.isEmpty()) {
                                        Map<String, Object> ActionMovedSection = (Map<String, Object>) firstAction.getActionDetails().get("ActionMovedSection");
                                        if (ActionMovedSection != null) {
                                            Integer workId = (Integer) ActionMovedSection.get("workId");
                                            taskDTO.setWorkId(workId);
                                        }
                                    }

                                    break;
                                }

                                case "Assignee is empty:Remove task from the project": {

                                    if(newCollaboratorIds.isEmpty()) {
                                        taskRepository.deleteById(taskDTO.getTaskId());
                                    }

                                    break;
                                }

                                case "Assignee is empty:Complete task": {

                                    if(newCollaboratorIds.isEmpty()) {
                                        taskDTO.setStatus(true);
                                    }

                                    break;
                                }

                                case "Assignee is empty:Incomplete task": {

                                    if(newCollaboratorIds.isEmpty()) {
                                        taskDTO.setStatus(false);
                                    }

                                    break;
                                }

                                case "Assignee is...:Move task to section": {

                                    Map<String, Object> assignee = (Map<String, Object>) firstTrigger.getTriggerDetails().get("assignee");
                                    Integer triggerMemberId = (Integer) assignee.get("id");

                                    newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                        Map<String, Object> ActionMovedSection = (Map<String, Object>) firstAction.getActionDetails().get("ActionMovedSection");
                                        if (ActionMovedSection != null) {
                                            Integer workId = (Integer) ActionMovedSection.get("workId");
                                            taskDTO.setWorkId(workId);
                                        }
                                    });

                                    break;
                                }

                                case "Assignee is...:Remove task from the project": {

                                    Map<String, Object> assignee = (Map<String, Object>) firstTrigger.getTriggerDetails().get("assignee");
                                    Integer triggerMemberId = (Integer) assignee.get("id");

                                    newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                        taskRepository.deleteById(taskDTO.getTaskId());
                                    });

                                    break;
                                }

                                case "Assignee is...:Complete task": {

                                    Map<String, Object> assignee = (Map<String, Object>) firstTrigger.getTriggerDetails().get("assignee");
                                    Integer triggerMemberId = (Integer) assignee.get("id");

                                    newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                        taskDTO.setStatus(true);
                                    });

                                    break;
                                }

                                case "Assignee is...:Incomplete task": {

                                    Map<String, Object> assignee = (Map<String, Object>) firstTrigger.getTriggerDetails().get("assignee");
                                    Integer triggerMemberId = (Integer) assignee.get("id");

                                    newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                        taskDTO.setStatus(false);
                                    });

                                    break;
                                }

                                case "Assignee is changed:Set assignee to": {

                                    if(!newCollaborators.isEmpty()) {
                                        Map<String, Object> assignee = (Map<String, Object>) firstAction.getActionDetails().get("assignee");
                                        if (assignee != null) {
                                            Integer assigneeId = (Integer) assignee.get("id");
                                            List<Integer> updatedCollaborators = taskDTO.getCollaboratorIds() != null
                                                    ? new ArrayList<>(taskDTO.getCollaboratorIds())
                                                    : new ArrayList<>();
                                            if (!updatedCollaborators.contains(assigneeId)) {
                                                updatedCollaborators.add(assigneeId);
                                                taskDTO.setCollaboratorIds(updatedCollaborators);
                                            }
                                        }
                                    }

                                    break;
                                }

                                case "Assignee is empty:Set assignee to": {

                                    if(newCollaboratorIds.isEmpty()) {
                                        Map<String, Object> assignee = (Map<String, Object>) firstAction.getActionDetails().get("assignee");
                                        if (assignee != null) {
                                            Integer assigneeId = (Integer) assignee.get("id");
                                            List<Integer> updatedCollaborators = taskDTO.getCollaboratorIds() != null
                                                    ? new ArrayList<>(taskDTO.getCollaboratorIds())
                                                    : new ArrayList<>();
                                            if (!updatedCollaborators.contains(assigneeId)) {
                                                updatedCollaborators.add(assigneeId);
                                                taskDTO.setCollaboratorIds(updatedCollaborators);
                                            }
                                        }
                                    }
                                    break;
                                }

                                case "Assignee is...:Set assignee to": {

                                    Map<String, Object> triggerAssignee = (Map<String, Object>) firstTrigger.getTriggerDetails().get("assignee");
                                    Integer triggerMemberId = (Integer) triggerAssignee.get("id");

                                    Map<String, Object> actionAssignee = (Map<String, Object>) firstAction.getActionDetails().get("assignee");
                                    Integer actionMemberId = (Integer) actionAssignee.get("id");

                                    newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                        newCollaboratorIds.add(actionMemberId);
                                        taskDTO.setCollaboratorIds(newCollaboratorIds);
                                    });

                                    break;
                                }

                                case "Assignee is changed:Clear assignee": {

                                    if(!newCollaborators.isEmpty()) {
                                        taskDTO.setCollaboratorIds(new ArrayList<>());
                                        taskDTO.setTeamIds(new ArrayList<>());
                                    }

                                    break;
                                }

                                case "Assignee is...:Clear assignee": {

                                    Map<String, Object> triggerAssignee = (Map<String, Object>) firstTrigger.getTriggerDetails().get("assignee");
                                    Integer triggerMemberId = (Integer) triggerAssignee.get("id");

                                    newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                        taskDTO.setCollaboratorIds(new ArrayList<>());
                                        taskDTO.setTeamIds(new ArrayList<>());
                                    });

                                    break;
                                }

                                case "Assignee is changed:Set task title": {

                                    if(!newCollaborators.isEmpty()) {
                                        String taskName = (String) firstAction.getActionDetails().get("taskName");
                                        taskDTO.setTaskName(taskName);
                                    }
                                    break;
                                }

                                case "Assignee is empty:Set task title": {

                                    if(newCollaborators.isEmpty()) {
                                        String taskName = (String) firstAction.getActionDetails().get("taskName");
                                        taskDTO.setTaskName(taskName);
                                    }
                                    break;
                                }

                                case "Assignee is...:Set task title": {

                                    Map<String, Object> triggerAssignee = (Map<String, Object>) firstTrigger.getTriggerDetails().get("assignee");
                                    Integer triggerMemberId = (Integer) triggerAssignee.get("id");

                                    newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                        String taskName = (String) firstAction.getActionDetails().get("taskName");
                                        taskDTO.setTaskName(taskName);
                                    });
                                    break;
                                }

                                case "Assignee is changed:Set task description": {

                                    if(!newCollaborators.isEmpty()) {
                                        String taskDescription = (String) firstAction.getActionDetails().get("taskDescription");
                                        taskDTO.setDescription(taskDescription);
                                    }
                                    break;
                                }

                                case "Assignee is empty:Set task description": {

                                    if(newCollaborators.isEmpty()) {
                                        String taskDescription = (String) firstAction.getActionDetails().get("taskDescription");
                                        taskDTO.setDescription(taskDescription);
                                    }
                                    break;
                                }

                                case "Assignee is...:Set task description": {

                                    Map<String, Object> triggerAssignee = (Map<String, Object>) firstTrigger.getTriggerDetails().get("assignee");
                                    Integer triggerMemberId = (Integer) triggerAssignee.get("id");

                                    newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                        String taskDescription = (String) firstAction.getActionDetails().get("taskDescription");
                                        taskDTO.setDescription(taskDescription);
                                    });
                                    break;
                                }

                                case "Assignee is changed:Set due date to": {

                                    if(!newCollaborators.isEmpty()) {
                                        String dateStr = (String) firstAction.getActionDetails().get("date");
                                        LocalDate date = LocalDate.parse(dateStr);
                                        taskDTO.setDueDate(date);
                                    }

                                    break;
                                }

                                case "Assignee is empty:Set due date to": {

                                    if(newCollaborators.isEmpty()) {
                                        String dateStr = (String) firstAction.getActionDetails().get("date");
                                        LocalDate date = LocalDate.parse(dateStr);
                                        taskDTO.setDueDate(date);
                                    }
                                    break;
                                }

                                case "Assignee is...:Set due date to": {

                                    Map<String, Object> triggerAssignee = (Map<String, Object>) firstTrigger.getTriggerDetails().get("assignee");
                                    Integer triggerMemberId = (Integer) triggerAssignee.get("id");

                                    newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                        String dateStr = (String) firstAction.getActionDetails().get("date");
                                        LocalDate date = LocalDate.parse(dateStr);
                                        taskDTO.setDueDate(date);
                                    });
                                    break;
                                }


                                case "Task is add from:Clear due date": {

                                    taskDTO.setDueDate(null);
                                    break;
                                }

                                case "Assignee is changed:Create task": {

                                    if(!newCollaborators.isEmpty()) {
                                        Map<String, Object> task = (Map<String, Object>) firstAction.getActionDetails().get("task");
                                        String taskName = (String) task.get("name");
                                        String description = (String) task.get("description");
                                        LocalDate dueDate = LocalDate.parse((String) task.get("dueDate"));
                                        List<Integer> collaboratorIds = (List<Integer>) task.get("collaboratorIds");
                                        List<Integer> teamIds = (List<Integer>) task.get("teamIds");
                                        String priority = (String) task.get("priority");

                                        System.out.println("Task: " + task);

                                        Map<String, Object> whichSection = (Map<String, Object>) firstAction.getActionDetails().get("whichSection");
                                        Integer ActionWorkId = (Integer) whichSection.get("workId");

                                        TaskDTO newTaskDTO = new TaskDTO();

                                        newTaskDTO.setTaskName(taskName);
                                        newTaskDTO.setDescription(description);
                                        newTaskDTO.setDueDate(dueDate);
                                        newTaskDTO.setCollaboratorIds(collaboratorIds);
                                        newTaskDTO.setTeamIds(teamIds);
                                        newTaskDTO.setTags(taskDTO.getTags());
                                        newTaskDTO.setPriority(TaskPriorityLevel.valueOf(priority.toUpperCase()));
                                        newTaskDTO.setProjectId(taskDTO.getProjectId());
                                        newTaskDTO.setWorkId(ActionWorkId);
                                        newTaskDTO.setAssignerId(taskDTO.getAssignerId());
                                        newTaskDTO.setStatus(false);

                                        taskRepository.save(modelMapper.map(newTaskDTO, Task.class));
                                        taskProducer.sendCreateTaskMessage(newTaskDTO.getTaskName(), newTaskDTO.getAssignerId(), newTaskDTO.getCollaboratorIds());
                                    }

                                    break;
                                }

                                case "Assignee is empty:Create task": {

                                    if(newCollaborators.isEmpty()) {
                                        Map<String, Object> task = (Map<String, Object>) firstAction.getActionDetails().get("task");
                                        String taskName = (String) task.get("name");
                                        String description = (String) task.get("description");
                                        LocalDate dueDate = LocalDate.parse((String) task.get("dueDate"));
                                        List<Integer> collaboratorIds = (List<Integer>) task.get("collaboratorIds");
                                        List<Integer> teamIds = (List<Integer>) task.get("teamIds");
                                        String priority = (String) task.get("priority");

                                        System.out.println("Task: " + task);

                                        Map<String, Object> whichSection = (Map<String, Object>) firstAction.getActionDetails().get("whichSection");
                                        Integer ActionWorkId = (Integer) whichSection.get("workId");

                                        TaskDTO newTaskDTO = new TaskDTO();

                                        newTaskDTO.setTaskName(taskName);
                                        newTaskDTO.setDescription(description);
                                        newTaskDTO.setDueDate(dueDate);
                                        newTaskDTO.setCollaboratorIds(collaboratorIds);
                                        newTaskDTO.setTeamIds(teamIds);
                                        newTaskDTO.setTags(taskDTO.getTags());
                                        newTaskDTO.setPriority(TaskPriorityLevel.valueOf(priority.toUpperCase()));
                                        newTaskDTO.setProjectId(taskDTO.getProjectId());
                                        newTaskDTO.setWorkId(ActionWorkId);
                                        newTaskDTO.setAssignerId(taskDTO.getAssignerId());
                                        newTaskDTO.setStatus(false);

                                        taskRepository.save(modelMapper.map(newTaskDTO, Task.class));
                                        taskProducer.sendCreateTaskMessage(newTaskDTO.getTaskName(), newTaskDTO.getAssignerId(), newTaskDTO.getCollaboratorIds());
                                    }
                                    break;
                                }

                                case "Assignee is...:Create task": {

                                    Map<String, Object> triggerAssignee = (Map<String, Object>) firstTrigger.getTriggerDetails().get("assignee");
                                    Integer triggerMemberId = (Integer) triggerAssignee.get("id");

                                    newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {


                                        Map<String, Object> task = (Map<String, Object>) firstAction.getActionDetails().get("task");
                                        String taskName = (String) task.get("name");
                                        String description = (String) task.get("description");
                                        LocalDate dueDate = LocalDate.parse((String) task.get("dueDate"));
                                        List<Integer> collaboratorIds = (List<Integer>) task.get("collaboratorIds");
                                        List<Integer> teamIds = (List<Integer>) task.get("teamIds");
                                        String priority = (String) task.get("priority");

                                        System.out.println("Task: " + task);

                                        Map<String, Object> whichSection = (Map<String, Object>) firstAction.getActionDetails().get("whichSection");
                                        Integer ActionWorkId = (Integer) whichSection.get("workId");

                                        TaskDTO newTaskDTO = new TaskDTO();

                                        newTaskDTO.setTaskName(taskName);
                                        newTaskDTO.setDescription(description);
                                        newTaskDTO.setDueDate(dueDate);
                                        newTaskDTO.setCollaboratorIds(collaboratorIds);
                                        newTaskDTO.setTeamIds(teamIds);
                                        newTaskDTO.setTags(taskDTO.getTags());
                                        newTaskDTO.setPriority(TaskPriorityLevel.valueOf(priority.toUpperCase()));
                                        newTaskDTO.setProjectId(taskDTO.getProjectId());
                                        newTaskDTO.setWorkId(ActionWorkId);
                                        newTaskDTO.setAssignerId(taskDTO.getAssignerId());
                                        newTaskDTO.setStatus(false);

                                        taskRepository.save(modelMapper.map(newTaskDTO, Task.class));
                                        taskProducer.sendCreateTaskMessage(newTaskDTO.getTaskName(), newTaskDTO.getAssignerId(), newTaskDTO.getCollaboratorIds());
                                    });
                                    break;
                                }

                                case "Duedate is changed:Move task to section": {
                                    System.out.println("Due date changed ===> Move task to section");

                                    Task task = modelMapper.map(taskRepository.findById(taskDTO.getTaskId()), Task.class);

                                    if(!task.getDueDate().isEqual(taskDTO.getDueDate())) {

                                        Map<String, Object> ActionMovedSection = (Map<String, Object>) firstAction.getActionDetails().get("ActionMovedSection");
                                        if (ActionMovedSection != null) {
                                            Integer workId = (Integer) ActionMovedSection.get("workId");
                                            taskDTO.setWorkId(workId);
                                        }
                                    }

                                    break;
                                }

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        // Save updated task
        Task updatedTask = modelMapper.map(taskDTO, Task.class);
        taskRepository.save(updatedTask);

        // Notify collaborator changes
        if (!newCollaborators.isEmpty()) {
            taskProducer.sendUpdateTaskMessage(taskDTO.getTaskName(), taskDTO.getAssignerId(), "New", newCollaborators);
        }

        if (!removedCollaborators.isEmpty()) {
            taskProducer.sendUpdateTaskMessage(taskDTO.getTaskName(), taskDTO.getAssignerId(), "Removed", removedCollaborators);
        }

        if (!unchangedCollaborators.isEmpty()) {
            taskProducer.sendUpdateTaskMessage(taskDTO.getTaskName(), taskDTO.getAssignerId(), "Existing", unchangedCollaborators);
        }

        return "Task updated successfully";
    }

    @Override
    public String movedAndUpdateTask(TaskDTO taskDTO) {

        List<PublishFlow> publishFlows = publishFlowRepository.findPublishFlowsByProjectId(taskDTO.getProjectId());

        if (publishFlows != null && !publishFlows.isEmpty()) {
            for (PublishFlow publishFlow : publishFlows) {
                if (publishFlow.getStatus().equals("active")) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();

                        List<TriggerDTO> triggers = mapper.readValue(
                                publishFlow.getTriggersJson(),
                                new TypeReference<List<TriggerDTO>>() {}
                        );

                        List<ActionDTO> actions = mapper.readValue(
                                publishFlow.getActionsJson(),
                                new TypeReference<List<ActionDTO>>() {}
                        );

                        if (!triggers.isEmpty() && !actions.isEmpty()) {
                            TriggerDTO firstTrigger = triggers.get(0);
                            ActionDTO firstAction = actions.get(0);

                            String triggerType = (String) firstTrigger.getTriggerDetails().get("triggerType");
                            String actionType = (String) firstAction.getActionDetails().get("actionType");

                            switch (triggerType + ":" + actionType) {
                                case "Section is:Set assignee to": {
                                    Map<String, Object> section = (Map<String, Object>) firstTrigger.getTriggerDetails().get("section");
                                    Integer triggerWorkId = (Integer) section.get("workId");

                                    Object work = workWebClient.get()
                                            .uri("/getWork/{id}", taskDTO.getWorkId())
                                            .retrieve()
                                            .bodyToMono(new ParameterizedTypeReference<Object>() {})
                                            .block();

                                    String workName = (String) ((Map<String, Object>) work).get("workName");
                                    String triggerWorkName = (String) section.get("workName");

                                    if (workName.equals(triggerWorkName)) {
                                        Map<String, Object> assignee = (Map<String, Object>) firstAction.getActionDetails().get("assignee");
                                        if (assignee != null) {
                                            Integer assigneeId = (Integer) assignee.get("id");
                                            List<Integer> updatedCollaborators = taskDTO.getCollaboratorIds() != null
                                                    ? new ArrayList<>(taskDTO.getCollaboratorIds())
                                                    : new ArrayList<>();
                                            if (!updatedCollaborators.contains(assigneeId)) {
                                                updatedCollaborators.add(assigneeId);
                                                taskDTO.setCollaboratorIds(updatedCollaborators);
                                            }
                                        }
                                    }
                                    break;
                                }
                                case "Section changed:Move task to section": {
                                    Map<String, Object> actionMovedSection = (Map<String, Object>) firstAction.getActionDetails().get("ActionMovedSection");
                                    Integer movedWorkId = (Integer) actionMovedSection.get("workId");
                                    taskDTO.setWorkId(movedWorkId);

                                    Task task = modelMapper.map(taskRepository.findById(taskDTO.getTaskId()), Task.class);
                                    Boolean status = task.isStatus();

                                    break;
                                }
                                case "Section is:Move task to section": {
                                    Map<String, Object> section = (Map<String, Object>) firstTrigger.getTriggerDetails().get("section");
                                    Integer triggerWorkId = (Integer) section.get("workId");
                                    Map<String, Object> actionMovedSection = (Map<String, Object>) firstAction.getActionDetails().get("ActionMovedSection");
                                    Integer movedWorkId = (Integer) actionMovedSection.get("workId");
                                    if (triggerWorkId.equals(taskDTO.getWorkId()) && movedWorkId != taskDTO.getWorkId()) {
                                        taskDTO.setWorkId(movedWorkId);
                                    }
                                    break;
                                }

                                case "Section changed:Set assignee to": {
                                    Map<String, Object> assignee = (Map<String, Object>) firstAction.getActionDetails().get("assignee");
                                    Integer assigneeId = (Integer) assignee.get("id");
                                    List<Integer> updatedCollaborators = taskDTO.getCollaboratorIds() != null
                                            ? new ArrayList<>(taskDTO.getCollaboratorIds())
                                            : new ArrayList<>();
                                    if (!updatedCollaborators.contains(assigneeId)) {
                                        updatedCollaborators.add(assigneeId);
                                        taskDTO.setCollaboratorIds(updatedCollaborators);
                                    }
                                    break;
                                }

                                case "Section is:Clear assignee": {
                                    Map<String, Object> section = (Map<String, Object>) firstTrigger.getTriggerDetails().get("section");
                                    Integer TriggerWorkId = (Integer) section.get("workId");

                                    if(TriggerWorkId.equals(taskDTO.getWorkId())) {
                                        taskDTO.setCollaboratorIds(new ArrayList<>());
                                        taskDTO.setTeamIds(new ArrayList<>());
                                    }
                                    break;
                                }
                                case "Section changed:Clear assignee": {
                                    taskDTO.setCollaboratorIds(new ArrayList<>());
                                    taskDTO.setTeamIds(new ArrayList<>());
                                    break;
                                }
                                case "Section is:Remove task from the project": {
                                    Map<String, Object> section = (Map<String, Object>) firstTrigger.getTriggerDetails().get("section");
                                    Integer triggerWorkId = (Integer) section.get("workId");
                                    if (triggerWorkId != null && triggerWorkId.equals(taskDTO.getWorkId())) {
                                        Task taskEntity = taskRepository.findById(taskDTO.getTaskId())
                                                .orElseThrow(() -> new RuntimeException("Task not found"));
                                        taskRepository.deleteById(taskEntity.getTaskId());
                                        taskProducer.sendDeleteTaskMessage(taskEntity.getTaskName(), taskEntity.getAssignerId(), taskEntity.getCollaboratorIds());
                                        return "Task deleted successfully";
                                    }
                                    break;
                                }
                                case "Section changed:Remove task from the project": {
                                    Task task = modelMapper.map(taskRepository.findById(taskDTO.getTaskId()), Task.class);
                                    if (!Objects.equals(task.getWorkId(), taskDTO.getWorkId())) {
                                        Task taskEntity = taskRepository.findById(taskDTO.getTaskId())
                                                .orElseThrow(() -> new RuntimeException("Task not found"));
                                        taskRepository.deleteById(taskEntity.getTaskId());
                                        taskProducer.sendDeleteTaskMessage(taskEntity.getTaskName(), taskEntity.getAssignerId(), taskEntity.getCollaboratorIds());
                                        return "Task deleted successfully";
                                    }
                                    break;
                                }
                                case "Section changed:Complete task": {
                                    taskDTO.setStatus(true);
                                    break;
                                }
                                case "Section is:Complete task": {
                                    Map<String, Object> section = (Map<String, Object>) firstTrigger.getTriggerDetails().get("section");
                                    Integer TriggerWorkId = (Integer) section.get("workId");

                                    if(TriggerWorkId.equals(taskDTO.getWorkId())) {
                                        taskDTO.setStatus(true);
                                    }
                                    break;
                                }

                                case "Section changed:Incomplete task": {
                                    taskDTO.setStatus(false);
                                    break;
                                }
                                case "Section is:Incomplete task": {
                                    Map<String, Object> section = (Map<String, Object>) firstTrigger.getTriggerDetails().get("section");
                                    Integer TriggerWorkId = (Integer) section.get("workId");

                                    if(TriggerWorkId.equals(taskDTO.getWorkId())) {
                                        taskDTO.setStatus(false);
                                    }
                                    break;
                                }
                                case "Section changed:Set task title": {
                                    String taskName = (String) firstAction.getActionDetails().get("taskName");
                                    taskDTO.setTaskName(taskName);
                                    break;
                                }
                                case "Section is:Set task title": {
                                    String taskName = (String) firstAction.getActionDetails().get("taskName");

                                    Map<String, Object> section = (Map<String, Object>) firstTrigger.getTriggerDetails().get("section");
                                    Integer TriggerWorkId = (Integer) section.get("workId");

                                    if(TriggerWorkId.equals(taskDTO.getWorkId())) {
                                        taskDTO.setTaskName(taskName);
                                    }
                                    break;
                                }
                                case "Section changed:Set task description": {
                                    String taskDescription = (String) firstAction.getActionDetails().get("taskDescription");
                                    taskDTO.setDescription(taskDescription);
                                    break;
                                }
                                case "Section is:Set task description": {
                                    String taskDescription = (String) firstAction.getActionDetails().get("taskDescription");

                                    Map<String, Object> section = (Map<String, Object>) firstTrigger.getTriggerDetails().get("section");
                                    Integer TriggerWorkId = (Integer) section.get("workId");

                                    if(TriggerWorkId.equals(taskDTO.getWorkId())) {
                                        taskDTO.setDescription(taskDescription);
                                    }
                                    break;
                                }
                                case "Section is:Set due date to": {
                                    String dateStr = (String) firstAction.getActionDetails().get("date");
                                    LocalDate date = LocalDate.parse(dateStr); // Parses ISO-8601 format like "2025-04-11"
                                    System.out.println("Task due date: " + date);

                                    Map<String, Object> section = (Map<String, Object>) firstTrigger.getTriggerDetails().get("section");
                                    Integer TriggerWorkId = (Integer) section.get("workId");

                                    if(TriggerWorkId.equals(taskDTO.getWorkId())) {
                                        taskDTO.setDueDate(date);
                                    }
                                    break;
                                }
                                case "Section changed:Set due date to": {
                                    String dateStr = (String) firstAction.getActionDetails().get("date");
                                    LocalDate date = LocalDate.parse(dateStr);
                                    taskDTO.setDueDate(date);
                                    break;
                                }
                                case "Section changed:Create task": {
                                    Map<String, Object> task = (Map<String, Object>) firstAction.getActionDetails().get("task");
                                    String taskName = (String) task.get("name");
                                    String description = (String) task.get("description");
                                    LocalDate dueDate = LocalDate.parse((String) task.get("dueDate"));
                                    List<Integer> collaboratorIds = (List<Integer>) task.get("collaboratorIds");
                                    List<Integer> teamIds = (List<Integer>) task.get("teamIds");
//                                    String tags = (String) task.get("tags");
                                    String priority = (String) task.get("priority");

                                    System.out.println("Task: " + task);

                                    Map<String, Object> whichSection = (Map<String, Object>) firstAction.getActionDetails().get("whichSection");
                                    Integer ActionWorkId = (Integer) whichSection.get("workId");

                                        TaskDTO newTaskDTO = new TaskDTO();

                                        newTaskDTO.setTaskName(taskName);
                                        newTaskDTO.setDescription(description);
                                        newTaskDTO.setDueDate(dueDate);
                                        newTaskDTO.setCollaboratorIds(collaboratorIds);
                                        newTaskDTO.setTeamIds(teamIds);
                                        newTaskDTO.setTags(taskDTO.getTags());
                                        newTaskDTO.setPriority(TaskPriorityLevel.valueOf(priority.toUpperCase()));
                                        newTaskDTO.setProjectId(taskDTO.getProjectId());
                                        newTaskDTO.setWorkId(ActionWorkId);
                                        newTaskDTO.setAssignerId(taskDTO.getAssignerId());
                                        newTaskDTO.setStatus(false);

                                        taskRepository.save(modelMapper.map(newTaskDTO, Task.class));
                                        taskProducer.sendCreateTaskMessage(newTaskDTO.getTaskName(), newTaskDTO.getAssignerId(), newTaskDTO.getCollaboratorIds());

                                break;
                                }


                                case "Section is:Create task": {
                                    Map<String, Object> task = (Map<String, Object>) firstAction.getActionDetails().get("task");
                                    String taskName = (String) task.get("name");
                                    String description = (String) task.get("description");
                                    LocalDate dueDate = LocalDate.parse((String) task.get("dueDate"));
                                    List<Integer> collaboratorIds = (List<Integer>) task.get("collaboratorIds");
                                    List<Integer> teamIds = (List<Integer>) task.get("teamIds");
                                    String priority = (String) task.get("priority");

                                    System.out.println("Task: " + task);

                                    Map<String, Object> whichSection = (Map<String, Object>) firstAction.getActionDetails().get("whichSection");
                                    Integer ActionWorkId = (Integer) whichSection.get("workId");

                                    Map<String, Object> section = (Map<String, Object>) firstTrigger.getTriggerDetails().get("section");
                                    Integer TriggerWorkId = (Integer) section.get("workId");
                                    if(TriggerWorkId.equals(taskDTO.getWorkId())) {
                                        TaskDTO newTaskDTO = new TaskDTO();

                                        newTaskDTO.setTaskName(taskName);
                                        newTaskDTO.setDescription(description);
                                        newTaskDTO.setDueDate(dueDate);
                                        newTaskDTO.setCollaboratorIds(collaboratorIds);
                                        newTaskDTO.setTeamIds(teamIds);
                                        newTaskDTO.setTags(taskDTO.getTags());
                                        newTaskDTO.setPriority(TaskPriorityLevel.valueOf(priority.toUpperCase()));
                                        newTaskDTO.setProjectId(taskDTO.getProjectId());
                                        newTaskDTO.setWorkId(ActionWorkId);
                                        newTaskDTO.setAssignerId(taskDTO.getAssignerId());
                                        newTaskDTO.setStatus(false);

                                        taskRepository.save(modelMapper.map(newTaskDTO, Task.class));
                                        taskProducer.sendCreateTaskMessage(newTaskDTO.getTaskName(), newTaskDTO.getAssignerId(), newTaskDTO.getCollaboratorIds());
                                    }
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        Task updatedTask = modelMapper.map(taskDTO, Task.class);
        taskRepository.save(updatedTask);

        return "Task updated successfully";
    }


    @Override
    public String deleteTask(int id) {
        Task task = modelMapper.map(getTaskById(id), Task.class);
        taskRepository.deleteById(id);

        taskProducer.sendDeleteTaskMessage(task.getTaskName(), task.getAssignerId(), task.getCollaboratorIds());
        return "Successfully deleted";
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
//        if (workIds.size() > taskIndex + 1) {
//            task.setWorkId(workIds.get(taskIndex + 1));
//        } else {
//            task.setStatus(true);
//        }

        // task.setStatus(true);
        // Save the updated task

        List<PublishFlow> publishFlows = publishFlowRepository.findPublishFlowsByProjectId(task.getProjectId());

        if (publishFlows != null && !publishFlows.isEmpty()) {
            for (PublishFlow publishFlow : publishFlows) {
                if (publishFlow.getStatus().equals("active")) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();

                        List<TriggerDTO> triggers = mapper.readValue(
                                publishFlow.getTriggersJson(),
                                new TypeReference<List<TriggerDTO>>() {
                                }
                        );

                        List<ActionDTO> actions = mapper.readValue(
                                publishFlow.getActionsJson(),
                                new TypeReference<List<ActionDTO>>() {
                                }
                        );

                        if (!triggers.isEmpty() && !actions.isEmpty()) {
                            TriggerDTO firstTrigger = triggers.get(0);
                            ActionDTO firstAction = actions.get(0);

                            String triggerType = (String) firstTrigger.getTriggerDetails().get("triggerType");
                            String actionType = (String) firstAction.getActionDetails().get("actionType");

                            switch (triggerType + ":" + actionType) {
                                case "Status is changed:Move task to section": {

                                    if(task.isStatus()) {
                                        Map<String, Object> ActionMovedSection = (Map<String, Object>) firstAction.getActionDetails().get("ActionMovedSection");
                                        if (ActionMovedSection != null) {
                                            Integer workId = (Integer) ActionMovedSection.get("workId");
                                            task.setWorkId(workId);
                                        }
                                    }

                                     else {
                                        Map<String, Object> ActionMovedSection = (Map<String, Object>) firstAction.getActionDetails().get("ActionMovedSection");
                                        if (ActionMovedSection != null) {
                                            Integer workId = (Integer) ActionMovedSection.get("workId");
                                            task.setWorkId(workId);
                                        }
                                    }
                                     break;
                                }

                                case "Status is incomplete:Move task to section": {

                                    if(task.isStatus()) {
                                        Map<String, Object> ActionMovedSection = (Map<String, Object>) firstAction.getActionDetails().get("ActionMovedSection");
                                        if (ActionMovedSection != null) {
                                            Integer workId = (Integer) ActionMovedSection.get("workId");
                                            task.setWorkId(workId);
                                        }
                                    }
                                    break;
                                }

                                case "Status is complete:Move task to section": {

                                    if(!task.isStatus()) {
                                        Map<String, Object> ActionMovedSection = (Map<String, Object>) firstAction.getActionDetails().get("ActionMovedSection");
                                        if (ActionMovedSection != null) {
                                            Integer workId = (Integer) ActionMovedSection.get("workId");
                                            task.setWorkId(workId);
                                        }
                                    }
                                    break;
                                }
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if(task.isStatus() == false) {
            task.setStatus(true);
        } else {
            task.setStatus(false);
        }

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
        System.out.println("Delete rule id: " + id);
        ruleRepository.deleteById(id);

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
//            List<PublishFlow> existingPublishFlows = publishFlowRepository.findAll();
//
//            // Check if a record with the same projectId exists
//            Optional<PublishFlow> matchedFlow = existingPublishFlows.stream()
//                    .filter(flow -> flow.getProjectId() == publishFlowDTO.getProjectId())
//                    .findFirst();
//
//            if (matchedFlow.isPresent()) {
//                // Update the existing record
//                PublishFlow flowToUpdate = matchedFlow.get();
//                flowToUpdate.setPublishFlowName(publishFlowDTO.getPublishFlowName());
//                flowToUpdate.setTriggers(publishFlowDTO.getTriggers());
//                flowToUpdate.setActions(publishFlowDTO.getActions());
//                flowToUpdate.setUpdatedAt(LocalDateTime.now());
//
//                publishFlowRepository.save(flowToUpdate);
//            } else {
                // Save as a new record
                PublishFlow newFlow = modelMapper.map(publishFlowDTO, PublishFlow.class);
                newFlow.setCreatedAt(LocalDateTime.now());
                newFlow.setUpdatedAt(LocalDateTime.now());
                publishFlowRepository.save(newFlow);
//            }

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
        List<PublishFlow> publishFlow = publishFlowRepository.findPublishFlowsByProjectId(projectId);
        if (publishFlow == null) {
            return null;
        }
        return modelMapper.map(publishFlow, PublishFlowDTO.class);
    }

    @Override
    public PublishFlowDTO findPublishFlowByRuleId(int ruleId) {
        PublishFlow publishFlow = publishFlowRepository.findPublishFlowByRuleId(ruleId);
        if (publishFlow == null) {
            return null;
        }
        return modelMapper.map(publishFlow, PublishFlowDTO.class);
    }

    @Override
    public void deletePublishFlowByRuleId(int ruleId) {
        publishFlowRepository.deletePublishFlowByRuleId(ruleId);
    }

}
