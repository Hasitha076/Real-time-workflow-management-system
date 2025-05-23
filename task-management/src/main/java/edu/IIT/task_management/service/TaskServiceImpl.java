package edu.IIT.task_management.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.IIT.task_management.dto.*;
import edu.IIT.task_management.model.*;
import edu.IIT.task_management.producer.TaskProducer;
import edu.IIT.task_management.repository.*;
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

        List<PublishFlow> publishFlows = publishFlowRepository.findPublishFlowsByProjectId(taskDTO.getProjectId());

        System.out.println("publishFlows: " + publishFlows);
        if (publishFlows != null && !publishFlows.isEmpty()) {
            for (PublishFlow publishFlow : publishFlows) {
                if ("active".equals(publishFlow.getStatus())) {
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

                        System.out.println("triggers: " + triggers);
                        System.out.println("triggers size: " + triggers.size());

                        System.out.println("actions: " + actions);
                        System.out.println("Action size: " + actions.size());

                        for (int i = 0; i < actions.size(); i++) {
                            TriggerDTO trigger = triggers.get(0);
                            ActionDTO action = actions.get(i);

                            String triggerType = (String) trigger.getTriggerDetails().get("triggerType");
                            Map<String, Object> triggerSection = (Map<String, Object>) trigger.getTriggerDetails().get("section");
                            String actionType = (String) action.getActionDetails().get("actionType");
                            String key = triggerType + ":" + actionType;

                            System.out.println("Key: " + key);

                            if (key.equals("All tasks:Set assignee to")) {

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("All tasks (Any Work) ===> Set assignee to");

                                    Map<String, Object> assignee = (Map<String, Object>) action.getActionDetails().get("assignee");
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
                                } else {
                                    System.out.println("All tasks (Specific Work) ===> Set assignee to");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                   if(taskDTO.getWorkId() == workId) {
                                        String templateName = (String) trigger.getTriggerDetails().get("taskTemplateName");

                                        Map<String, Object> assignee = (Map<String, Object>) action.getActionDetails().get("assignee");
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
                                }
                            }

                            if (key.equals("Task is added from:Set assignee to")) {
                                System.out.println("Task is add from ===> Set assignee to");

                                Map<String, Object> template = (Map<String, Object>) trigger.getTriggerDetails().get("taskTemplate");
                                String templateName = (String) template.get("taskTemplateName");

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("Task is added from (Any Work) ===> Set assignee to");

                                    System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                    System.out.println("Template Name: " + templateName);

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        Map<String, Object> assignee = (Map<String, Object>) action.getActionDetails().get("assignee");
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
                                }

                                else {
                                    System.out.println("Task is added from (Specific Work) ===> Set assignee to");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                        System.out.println("Template Name: " + templateName);

                                        if(taskDTO.getWorkId() == workId) {
                                            Map<String, Object> assignee = (Map<String, Object>) action.getActionDetails().get("assignee");
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
                                    }
                                }
                            }


                            if (key.equals("All tasks:Move task to section")) {

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("All tasks (Any Work) ===> Move task to section");

                                    Map<String, Object> ActionMovedSection = (Map<String, Object>) action.getActionDetails().get("ActionMovedSection");
                                    if (ActionMovedSection != null) {
                                        Integer workId = (Integer) ActionMovedSection.get("workId");
                                        taskDTO.setWorkId(workId);
                                    }

                                } else {
                                    System.out.println("All tasks (Specific Work) ===> Move task to section");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getWorkId() == workId) {
                                        String templateName = (String) trigger.getTriggerDetails().get("taskTemplateName");

                                        Map<String, Object> ActionMovedSection = (Map<String, Object>) action.getActionDetails().get("ActionMovedSection");
                                        if (ActionMovedSection != null) {
                                            Integer workid = (Integer) ActionMovedSection.get("workId");
                                            taskDTO.setWorkId(workid);


                                        }
                                    }
                                }
                            }

                            if (key.equals("Task is added from:Move task to section")) {
                                System.out.println("Task is add from ===> Move task to section");

                                Map<String, Object> template = (Map<String, Object>) trigger.getTriggerDetails().get("taskTemplate");
                                String templateName = (String) template.get("taskTemplateName");

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("Task is added from (Any Work) ===> Move task to section");

                                    System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                    System.out.println("Template Name: " + templateName);

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        Map<String, Object> ActionMovedSection = (Map<String, Object>) action.getActionDetails().get("ActionMovedSection");
                                        if (ActionMovedSection != null) {
                                            Integer workId = (Integer) ActionMovedSection.get("workId");
                                            taskDTO.setWorkId(workId);
                                        }
                                    }
                                }

                                else {
                                    System.out.println("Task is added from (Specific Work) ===> SMove task to section");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                        System.out.println("Template Name: " + templateName);

                                        if(taskDTO.getWorkId() == workId) {
                                            Map<String, Object> ActionMovedSection = (Map<String, Object>) action.getActionDetails().get("ActionMovedSection");
                                            if (ActionMovedSection != null) {
                                                Integer workid = (Integer) ActionMovedSection.get("workId");
                                                taskDTO.setWorkId(workid);
                                            }
                                        }
                                    }
                                }
                            }


                            if (key.equals("All tasks:Clear assignee")) {

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("All tasks (Any Work) ===> Clear assignee");

                                    taskDTO.setCollaboratorIds(new ArrayList<>());
                                    taskDTO.setTeamIds(new ArrayList<>());

                                } else {
                                    System.out.println("All tasks (Specific Work) ===> Clear assignee");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getWorkId() == workId) {
                                        String templateName = (String) trigger.getTriggerDetails().get("taskTemplateName");

                                        taskDTO.setCollaboratorIds(new ArrayList<>());
                                        taskDTO.setTeamIds(new ArrayList<>());
                                    }
                                }
                            }

                            if (key.equals("Task is added from:Clear assignee")) {
                                System.out.println("Task is add from ===> Clear assignee");

                                Map<String, Object> template = (Map<String, Object>) trigger.getTriggerDetails().get("taskTemplate");
                                String templateName = (String) template.get("taskTemplateName");

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("Task is added from (Any Work) ===> Clear assignee");

                                    System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                    System.out.println("Template Name: " + templateName);

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        taskDTO.setCollaboratorIds(new ArrayList<>());
                                        taskDTO.setTeamIds(new ArrayList<>());
                                    }
                                }

                                else {
                                    System.out.println("Task is added from (Specific Work) ===> Clear assignee");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                        System.out.println("Template Name: " + templateName);

                                        if(taskDTO.getWorkId() == workId) {
                                            taskDTO.setCollaboratorIds(new ArrayList<>());
                                            taskDTO.setTeamIds(new ArrayList<>());
                                        }
                                    }
                                }
                            }


                            if (key.equals("All tasks:Complete task")) {

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("All tasks (Any Work) ===> Complete task");

                                    taskDTO.setStatus(true);

                                } else {
                                    System.out.println("All tasks (Specific Work) ===> Complete task");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getWorkId() == workId) {
                                        taskDTO.setStatus(true);
                                    }
                                }
                            }

                            if (key.equals("Task is added from:Complete task")) {
                                System.out.println("Task is add from ===> Complete task");

                                Map<String, Object> template = (Map<String, Object>) trigger.getTriggerDetails().get("taskTemplate");
                                String templateName = (String) template.get("taskTemplateName");

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("Task is added from (Any Work) ===> Complete task");

                                    System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                    System.out.println("Template Name: " + templateName);

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        taskDTO.setStatus(true);
                                    }
                                }

                                else {
                                    System.out.println("Task is added from (Specific Work) ===> Complete task");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                        System.out.println("Template Name: " + templateName);

                                        if(taskDTO.getWorkId() == workId) {
                                            taskDTO.setStatus(true);
                                        }
                                    }
                                }
                            }


                            if (key.equals("All tasks:Incomplete task")) {

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("All tasks (Any Work) ===> Incomplete task");

                                    taskDTO.setStatus(false);

                                } else {
                                    System.out.println("All tasks (Specific Work) ===> Complete task");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getWorkId() == workId) {
                                        String templateName = (String) trigger.getTriggerDetails().get("taskTemplateName");

                                        taskDTO.setStatus(false);
                                    }
                                }
                            }

                            if (key.equals("Task is added from:Incomplete task")) {
                                System.out.println("Task is add from ===> Incomplete task");

                                Map<String, Object> template = (Map<String, Object>) trigger.getTriggerDetails().get("taskTemplate");
                                String templateName = (String) template.get("taskTemplateName");

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("Task is added from (Any Work) ===> Incomplete task");

                                    System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                    System.out.println("Template Name: " + templateName);

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        taskDTO.setStatus(false);
                                    }
                                }

                                else {
                                    System.out.println("Task is added from (Specific Work) ===> Incomplete task");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                        System.out.println("Template Name: " + templateName);

                                        if(taskDTO.getWorkId() == workId) {
                                            taskDTO.setStatus(false);
                                        }
                                    }
                                }
                            }


                            if (key.equals("All tasks:Set task title")) {

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("All tasks (Any Work) ===> Set task title");

                                    String taskName = (String) action.getActionDetails().get("taskName");
                                    taskDTO.setTaskName(taskName);

                                } else {
                                    System.out.println("All tasks (Specific Work) ===> Set task title");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getWorkId() == workId) {
                                        String templateName = (String) trigger.getTriggerDetails().get("taskTemplateName");

                                        String taskName = (String) action.getActionDetails().get("taskName");
                                        taskDTO.setTaskName(taskName);
                                    }
                                }
                            }

                            if (key.equals("Task is added from:Set task title")) {
                                System.out.println("Task is add from ===> Set task title");

                                Map<String, Object> template = (Map<String, Object>) trigger.getTriggerDetails().get("taskTemplate");
                                String templateName = (String) template.get("taskTemplateName");

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("Task is added from (Any Work) ===> Set task title");

                                    System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                    System.out.println("Template Name: " + templateName);

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        String taskName = (String) action.getActionDetails().get("taskName");
                                        taskDTO.setTaskName(taskName);
                                    }
                                }

                                else {
                                    System.out.println("Task is added from (Specific Work) ===> Set task title");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                        System.out.println("Template Name: " + templateName);

                                        if(taskDTO.getWorkId() == workId) {
                                            String taskName = (String) action.getActionDetails().get("taskName");
                                            taskDTO.setTaskName(taskName);
                                        }
                                    }
                                }
                            }


                            if (key.equals("All tasks:Set task description")) {

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("All tasks (Any Work) ===> Set task description");

                                    String taskDescription = (String) action.getActionDetails().get("taskDescription");
                                    taskDTO.setDescription(taskDescription);

                                } else {
                                    System.out.println("All tasks (Specific Work) ===> Set task description");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getWorkId() == workId) {
                                        String templateName = (String) trigger.getTriggerDetails().get("taskTemplateName");

                                        String taskDescription = (String) action.getActionDetails().get("taskDescription");
                                        taskDTO.setDescription(taskDescription);
                                    }
                                }
                            }

                            if (key.equals("Task is added from:Set task description")) {
                                System.out.println("Task is add from ===> Set task description");

                                Map<String, Object> template = (Map<String, Object>) trigger.getTriggerDetails().get("taskTemplate");
                                String templateName = (String) template.get("taskTemplateName");

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("Task is added from (Any Work) ===> Set task description");

                                    System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                    System.out.println("Template Name: " + templateName);

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        String taskDescription = (String) action.getActionDetails().get("taskDescription");
                                        taskDTO.setDescription(taskDescription);
                                    }
                                }

                                else {
                                    System.out.println("Task is added from (Specific Work) ===> Set task description");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                        System.out.println("Template Name: " + templateName);

                                        if(taskDTO.getWorkId() == workId) {
                                            String taskDescription = (String) action.getActionDetails().get("taskDescription");
                                            taskDTO.setDescription(taskDescription);
                                        }
                                    }
                                }
                            }


                            if (key.equals("All tasks:Set due date to")) {

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("All tasks (Any Work) ===> Set due date to");

                                    String dateStr = (String) action.getActionDetails().get("date");
                                    LocalDate date = LocalDate.parse(dateStr);
                                    taskDTO.setDueDate(date);

                                } else {
                                    System.out.println("All tasks (Specific Work) ===> Set due date to");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getWorkId() == workId) {
                                        String templateName = (String) trigger.getTriggerDetails().get("taskTemplateName");

                                        String dateStr = (String) action.getActionDetails().get("date");
                                        LocalDate date = LocalDate.parse(dateStr);
                                        taskDTO.setDueDate(date);
                                    }
                                }
                            }

                            if (key.equals("Task is added from:Set due date to")) {
                                System.out.println("Task is add from ===> Set due date to");

                                Map<String, Object> template = (Map<String, Object>) trigger.getTriggerDetails().get("taskTemplate");
                                String templateName = (String) template.get("taskTemplateName");

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("Task is added from (Any Work) ===> Set due date to");

                                    System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                    System.out.println("Template Name: " + templateName);

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        String dateStr = (String) action.getActionDetails().get("date");
                                        LocalDate date = LocalDate.parse(dateStr);
                                        taskDTO.setDueDate(date);
                                    }
                                }

                                else {
                                    System.out.println("Task is added from (Specific Work) ===> Set due date to");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                        System.out.println("Template Name: " + templateName);

                                        if(taskDTO.getWorkId() == workId) {
                                            String dateStr = (String) action.getActionDetails().get("date");
                                            LocalDate date = LocalDate.parse(dateStr);
                                            taskDTO.setDueDate(date);
                                        }
                                    }
                                }
                            }


                            if (key.equals("All tasks:Clear due date")) {

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("All tasks (Any Work) ===> Clear due date");

                                    taskDTO.setDueDate(null);

                                } else {
                                    System.out.println("All tasks (Specific Work) ===> Clear due date");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getWorkId() == workId) {
                                        String templateName = (String) trigger.getTriggerDetails().get("taskTemplateName");

                                        taskDTO.setDueDate(null);
                                    }
                                }
                            }

                            if (key.equals("Task is added from:Clear due date")) {
                                System.out.println("Task is add from ===> Clear due date");

                                Map<String, Object> template = (Map<String, Object>) trigger.getTriggerDetails().get("taskTemplate");
                                String templateName = (String) template.get("taskTemplateName");

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("Task is added from (Any Work) ===> Clear due date");

                                    System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                    System.out.println("Template Name: " + templateName);

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        taskDTO.setDueDate(null);
                                    }
                                }

                                else {
                                    System.out.println("Task is added from (Specific Work) ===> Clear due date");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                        System.out.println("Template Name: " + templateName);

                                        if(taskDTO.getWorkId() == workId) {
                                            taskDTO.setDueDate(null);
                                        }
                                    }
                                }
                            }

                            if (key.equals("All tasks:Create task")) {

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("All tasks (Any Work) ===> Create task");

                                    Map<String, Object> task = (Map<String, Object>) action.getActionDetails().get("task");
                                    String taskName = (String) task.get("name");
                                    String description = (String) task.get("description");
                                    LocalDate dueDate = LocalDate.parse((String) task.get("dueDate"));
                                    List<Integer> collaboratorIds = (List<Integer>) task.get("collaboratorIds");
                                    List<Integer> teamIds = (List<Integer>) task.get("teamIds");
                                    String priority = (String) task.get("priority");

                                    System.out.println("Task: " + task);

                                    Map<String, Object> whichSection = (Map<String, Object>) action.getActionDetails().get("whichSection");
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

                                } else {
                                    System.out.println("All tasks (Specific Work) ===> Create task");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getWorkId() == workId) {
                                        String templateName = (String) trigger.getTriggerDetails().get("taskTemplateName");

                                        Map<String, Object> task = (Map<String, Object>) action.getActionDetails().get("task");
                                        String taskName = (String) task.get("name");
                                        String description = (String) task.get("description");
                                        LocalDate dueDate = LocalDate.parse((String) task.get("dueDate"));
                                        List<Integer> collaboratorIds = (List<Integer>) task.get("collaboratorIds");
                                        List<Integer> teamIds = (List<Integer>) task.get("teamIds");
                                        String priority = (String) task.get("priority");

                                        System.out.println("Task: " + task);

                                        Map<String, Object> whichSection = (Map<String, Object>) action.getActionDetails().get("whichSection");
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
                                }
                            }

                            if (key.equals("Task is added from:Create task")) {
                                System.out.println("Task is add from ===> Create task");

                                Map<String, Object> template = (Map<String, Object>) trigger.getTriggerDetails().get("taskTemplate");
                                String templateName = (String) template.get("taskTemplateName");

                                if (triggerSection.get("workName").equals("Any Work")) {
                                    System.out.println("Task is added from (Any Work) ===> Create task");

                                    System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                    System.out.println("Template Name: " + templateName);

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        Map<String, Object> task = (Map<String, Object>) action.getActionDetails().get("task");
                                        String taskName = (String) task.get("name");
                                        String description = (String) task.get("description");
                                        LocalDate dueDate = LocalDate.parse((String) task.get("dueDate"));
                                        List<Integer> collaboratorIds = (List<Integer>) task.get("collaboratorIds");
                                        List<Integer> teamIds = (List<Integer>) task.get("teamIds");
                                        String priority = (String) task.get("priority");

                                        System.out.println("Task: " + task);

                                        Map<String, Object> whichSection = (Map<String, Object>) action.getActionDetails().get("whichSection");
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
                                }

                                else {
                                    System.out.println("Task is added from (Specific Work) ===> Create task");
                                    Integer workId = (Integer) triggerSection.get("workId");

                                    if(taskDTO.getTaskName().equals(templateName)) {
                                        System.out.println("DTO task Name: " + taskDTO.getTaskName());
                                        System.out.println("Template Name: " + templateName);

                                        if(taskDTO.getWorkId() == workId) {
                                            Map<String, Object> task = (Map<String, Object>) action.getActionDetails().get("task");
                                            String taskName = (String) task.get("name");
                                            String description = (String) task.get("description");
                                            LocalDate dueDate = LocalDate.parse((String) task.get("dueDate"));
                                            List<Integer> collaboratorIds = (List<Integer>) task.get("collaboratorIds");
                                            List<Integer> teamIds = (List<Integer>) task.get("teamIds");
                                            String priority = (String) task.get("priority");

                                            System.out.println("Task: " + task);

                                            Map<String, Object> whichSection = (Map<String, Object>) action.getActionDetails().get("whichSection");
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
                                    }
                                }
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (taskDTO.getCollaboratorIds() == null) {
            CollaboratorsBlock collaboratorsBlocks = modelMapper.map(
                    taskRepository.findByWorkId(taskDTO.getWorkId()),
                    new TypeToken<CollaboratorsBlock>() {}.getType()
            );

            List<Integer> collaboratorIds = collaboratorsBlocks.getMemberIds();

            System.out.println(collaboratorIds);

            taskDTO.setCollaboratorIds(collaboratorIds);
        }

        taskRepository.save(modelMapper.map(taskDTO, Task.class));
        taskProducer.sendCreateTaskMessage(taskDTO.getTaskName(), taskDTO.getAssignerId(), taskDTO.getCollaboratorIds());
        return "Task created successfully";
    }

    @Override
    public TaskDTO getTaskById(int id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        if (taskOptional.isPresent()) {
            return modelMapper.map(taskOptional.get(), TaskDTO.class);
        } else {
            return null; // or throw custom NotFoundException
        }
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
//        if (taskDTO.getComments() != null) {
//            List<String> commentList = new ArrayList<>(existingTask.getComments() != null ? existingTask.getComments() : new ArrayList<>());
//            commentList.addAll(taskDTO.getComments());
//            taskDTO.setComments(commentList);
//        } else {
//            taskDTO.setComments(existingTask.getComments());
//        }

        if (taskDTO.getComments() != null) {

            // Preserve existing comments
            List<TaskCommentDTO> existingComments = existingTask.getComments() != null
                    ? existingTask.getComments()
                    : new ArrayList<>();

            // Create a new list with only the comment text (strip out userId)
            List<TaskCommentDTO> mergedComments = new ArrayList<>(existingComments);

            for (TaskCommentDTO newComment : taskDTO.getComments()) {
                TaskCommentDTO cleanedComment = new TaskCommentDTO();
                cleanedComment.setComment(newComment.getComment()); // only keep comment text
                cleanedComment.setUserId(newComment.getUserId());
                mergedComments.add(cleanedComment);
            }

            taskDTO.setComments(mergedComments);
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

        System.out.println("publishFlows: " + publishFlows);
        if (publishFlows != null && !publishFlows.isEmpty()) {
            for (PublishFlow publishFlow : publishFlows) {
                if ("active".equals(publishFlow.getStatus())) {
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

                        System.out.println("triggers: " + triggers);
                        System.out.println("triggers size: " + triggers.size());

                        System.out.println("actions: " + actions);
                        System.out.println("Action size: " + actions.size());

                        for (int i = 0; i < actions.size(); i++) {
                            TriggerDTO trigger = triggers.get(0);
                            ActionDTO action = actions.get(i);

                            String triggerType = (String) trigger.getTriggerDetails().get("triggerType");
                            String actionType = (String) action.getActionDetails().get("actionType");
                            String key = triggerType + ":" + actionType;

                            System.out.println("Key: " + key);


                            if (key.equals("Assignee is changed:Move task to section")) {
                                System.out.println("Assignee is changed ===> Move task to section");

                                if(!newCollaborators.isEmpty()) {
                                    Map<String, Object> ActionMovedSection = (Map<String, Object>) action.getActionDetails().get("ActionMovedSection");
                                    if (ActionMovedSection != null) {
                                        Integer workId = (Integer) ActionMovedSection.get("workId");
                                        taskDTO.setWorkId(workId);
                                    }
                                }
                            }

                            if (key.equals("Assignee is changed:Remove task from the project")) {
                                System.out.println("Assignee is changed ===> Remove task from the project");

                                if(!newCollaborators.isEmpty()) {
                                    taskRepository.deleteById(taskDTO.getTaskId());
                                }
                            }

                            if (key.equals("Assignee is changed:Complete task")) {
                                System.out.println("Assignee is changed ===> Complete task");

                                if(!newCollaborators.isEmpty()) {
                                    taskDTO.setStatus(true);
                                }
                            }

                            if (key.equals("Assignee is changed:Incomplete task")) {
                                System.out.println("Assignee is changed ===> Incomplete task");

                                if(!newCollaborators.isEmpty()) {
                                    taskDTO.setStatus(false);
                                }
                            }

                            if (key.equals("Assignee is empty:Move task to section")) {
                                System.out.println("Assignee is empty ===> Move task to section");

                                if(newCollaboratorIds.isEmpty()) {
                                    Map<String, Object> ActionMovedSection = (Map<String, Object>) action.getActionDetails().get("ActionMovedSection");
                                    if (ActionMovedSection != null) {
                                        Integer workId = (Integer) ActionMovedSection.get("workId");
                                        taskDTO.setWorkId(workId);
                                    }
                                }
                            }

                            if (key.equals("Assignee is empty:Remove task from the project")) {
                                System.out.println("Assignee is empty ===> Remove task from the project");

                                if(newCollaboratorIds.isEmpty()) {
                                    taskRepository.deleteById(taskDTO.getTaskId());
                                }
                            }

                            if (key.equals("Assignee is empty:Complete task")) {
                                System.out.println("Assignee is empty ===> Complete task");

                                if(newCollaboratorIds.isEmpty()) {
                                    taskDTO.setStatus(true);
                                }
                            }

                            if (key.equals("Assignee is empty:Incomplete task")) {
                                System.out.println("Assignee is empty ===> Incomplete task");

                                if(newCollaboratorIds.isEmpty()) {
                                    taskDTO.setStatus(false);
                                }
                            }

                            if (key.equals("Assignee is...:Move task to section")) {
                                System.out.println("Assignee is... ===> Move task to section");

                                Map<String, Object> assignee = (Map<String, Object>) trigger.getTriggerDetails().get("assignee");
                                Integer triggerMemberId = (Integer) assignee.get("id");

                                newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                    Map<String, Object> ActionMovedSection = (Map<String, Object>) action.getActionDetails().get("ActionMovedSection");
                                    if (ActionMovedSection != null) {
                                        Integer workId = (Integer) ActionMovedSection.get("workId");
                                        taskDTO.setWorkId(workId);
                                    }
                                });
                            }

                            if (key.equals("Assignee is...:Remove task from the project")) {
                                System.out.println("Assignee is... ===> Remove task from the project");

                                Map<String, Object> assignee = (Map<String, Object>) trigger.getTriggerDetails().get("assignee");
                                Integer triggerMemberId = (Integer) assignee.get("id");

                                newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                    taskRepository.deleteById(taskDTO.getTaskId());
                                });
                            }

                            if (key.equals("Assignee is...:Complete task")) {
                                System.out.println("Assignee is... ===> Complete task");

                                Map<String, Object> assignee = (Map<String, Object>) trigger.getTriggerDetails().get("assignee");
                                Integer triggerMemberId = (Integer) assignee.get("id");

                                newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                    taskDTO.setStatus(true);
                                });
                            }

                            if (key.equals("Assignee is...:Incomplete task")) {
                                System.out.println("Assignee is... ===> Incomplete task");

                                Map<String, Object> assignee = (Map<String, Object>) trigger.getTriggerDetails().get("assignee");
                                Integer triggerMemberId = (Integer) assignee.get("id");

                                newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                    taskDTO.setStatus(false);
                                });
                            }

                            if (key.equals("Assignee is changed:Set assignee to")) {
                                System.out.println("Assignee is changed ===> Set assignee to");

                                if(!newCollaborators.isEmpty()) {
                                    Map<String, Object> assignee = (Map<String, Object>) action.getActionDetails().get("assignee");
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
                            }

                            if (key.equals("Assignee is empty:Set assignee to")) {
                                System.out.println("Assignee is empty ===> Set assignee to");

                                if(newCollaboratorIds.isEmpty()) {
                                    Map<String, Object> assignee = (Map<String, Object>) action.getActionDetails().get("assignee");
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
                            }

                            if (key.equals("Assignee is...:Set assignee to")) {
                                System.out.println("Assignee is... ===> Set assignee to");

                                Map<String, Object> triggerAssignee = (Map<String, Object>) trigger.getTriggerDetails().get("assignee");
                                Integer triggerMemberId = (Integer) triggerAssignee.get("id");

                                Map<String, Object> actionAssignee = (Map<String, Object>) action.getActionDetails().get("assignee");
                                Integer actionMemberId = (Integer) actionAssignee.get("id");

                                newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                    newCollaboratorIds.add(actionMemberId);
                                    taskDTO.setCollaboratorIds(newCollaboratorIds);
                                });
                            }

                            if (key.equals("Assignee is changed:Clear assignee")) {
                                System.out.println("Assignee is changed ===> Clear assignee");

                                if(!newCollaborators.isEmpty()) {
                                    taskDTO.setCollaboratorIds(new ArrayList<>());
                                    taskDTO.setTeamIds(new ArrayList<>());
                                }
                            }

                            if (key.equals("Assignee is...:Clear assignee")) {
                                System.out.println("Assignee is... ===> Clear assignee");

                                Map<String, Object> triggerAssignee = (Map<String, Object>) trigger.getTriggerDetails().get("assignee");
                                Integer triggerMemberId = (Integer) triggerAssignee.get("id");

                                newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                    taskDTO.setCollaboratorIds(new ArrayList<>());
                                    taskDTO.setTeamIds(new ArrayList<>());
                                });
                            }

                            if (key.equals("Assignee is changed:Set task title")) {
                                System.out.println("Assignee is changed ===> Set task title");

                                if(!newCollaborators.isEmpty()) {
                                    String taskName = (String) action.getActionDetails().get("taskName");
                                    taskDTO.setTaskName(taskName);
                                }
                            }

                            if (key.equals("Assignee is empty:Set task title")) {
                                System.out.println("Assignee is empty ===> Set task title");

                                if(newCollaborators.isEmpty()) {
                                    String taskName = (String) action.getActionDetails().get("taskName");
                                    taskDTO.setTaskName(taskName);
                                }
                            }

                            if (key.equals("Assignee is...:Set task title")) {
                                System.out.println("Assignee is... ===> Set task title");

                                Map<String, Object> triggerAssignee = (Map<String, Object>) trigger.getTriggerDetails().get("assignee");
                                Integer triggerMemberId = (Integer) triggerAssignee.get("id");

                                newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                    String taskName = (String) action.getActionDetails().get("taskName");
                                    taskDTO.setTaskName(taskName);
                                });
                            }

                            if (key.equals("Assignee is changed:Set task description")) {
                                System.out.println("Assignee is changed ===> Set task description");

                                if(!newCollaborators.isEmpty()) {
                                    String taskDescription = (String) action.getActionDetails().get("taskDescription");
                                    taskDTO.setDescription(taskDescription);
                                }
                            }

                            if (key.equals("Assignee is empty:Set task description")) {
                                System.out.println("Assignee is empty ===> Set task description");

                                if(newCollaborators.isEmpty()) {
                                    String taskDescription = (String) action.getActionDetails().get("taskDescription");
                                    taskDTO.setDescription(taskDescription);
                                }
                            }

                            if (key.equals("Assignee is...:Set task description")) {
                                System.out.println("Assignee is... ===> Set task description");

                                Map<String, Object> triggerAssignee = (Map<String, Object>) trigger.getTriggerDetails().get("assignee");
                                Integer triggerMemberId = (Integer) triggerAssignee.get("id");

                                newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                    String taskDescription = (String) action.getActionDetails().get("taskDescription");
                                    taskDTO.setDescription(taskDescription);
                                });
                            }

                            if (key.equals("Assignee is changed:Set due date to")) {
                                System.out.println("Assignee is changed ===> Set due date to");

                                if(!newCollaborators.isEmpty()) {
                                    String dateStr = (String) action.getActionDetails().get("date");
                                    LocalDate date = LocalDate.parse(dateStr);
                                    taskDTO.setDueDate(date);
                                }
                            }

                            if (key.equals("Assignee is empty:Set due date to")) {
                                System.out.println("Assignee is empty ===> Set due date to");

                                if(newCollaborators.isEmpty()) {
                                    String dateStr = (String) action.getActionDetails().get("date");
                                    LocalDate date = LocalDate.parse(dateStr);
                                    taskDTO.setDueDate(date);
                                }
                            }

                            if (key.equals("Assignee is...:Set due date to")) {
                                System.out.println("Assignee is... ===> Set due date to");

                                Map<String, Object> triggerAssignee = (Map<String, Object>) trigger.getTriggerDetails().get("assignee");
                                Integer triggerMemberId = (Integer) triggerAssignee.get("id");

                                newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                    String dateStr = (String) action.getActionDetails().get("date");
                                    LocalDate date = LocalDate.parse(dateStr);
                                    taskDTO.setDueDate(date);
                                });
                            }

                            if (key.equals("Assignee is...:Clear due date")) {
                                System.out.println("Task is add from ===> Clear due date");

                                taskDTO.setDueDate(null);
                            }

                            if (key.equals("Assignee is changed:Create task")) {
                                System.out.println("Assignee is changed ===> Create task");

                                if (!newCollaborators.isEmpty()) {
                                    Map<String, Object> task = (Map<String, Object>) action.getActionDetails().get("task");
                                    String taskName = (String) task.get("name");
                                    String description = (String) task.get("description");
                                    LocalDate dueDate = LocalDate.parse((String) task.get("dueDate"));
                                    List<Integer> collaboratorIds = (List<Integer>) task.get("collaboratorIds");
                                    List<Integer> teamIds = (List<Integer>) task.get("teamIds");
                                    String priority = (String) task.get("priority");

                                    System.out.println("Task: " + task);

                                    Map<String, Object> whichSection = (Map<String, Object>) action.getActionDetails().get("whichSection");
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

                            }

                            if (key.equals("Assignee is empty:Create task")) {
                                System.out.println("Assignee is empty ===> Create task");

                                if (newCollaborators.isEmpty()) {
                                    Map<String, Object> task = (Map<String, Object>) action.getActionDetails().get("task");
                                    String taskName = (String) task.get("name");
                                    String description = (String) task.get("description");
                                    LocalDate dueDate = LocalDate.parse((String) task.get("dueDate"));
                                    List<Integer> collaboratorIds = (List<Integer>) task.get("collaboratorIds");
                                    List<Integer> teamIds = (List<Integer>) task.get("teamIds");
                                    String priority = (String) task.get("priority");

                                    System.out.println("Task: " + task);

                                    Map<String, Object> whichSection = (Map<String, Object>) action.getActionDetails().get("whichSection");
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
                            }

                            if (key.equals("Assignee is...:Create task")) {
                                System.out.println("Assignee is... ===> Create task");

                                Map<String, Object> triggerAssignee = (Map<String, Object>) trigger.getTriggerDetails().get("assignee");
                                Integer triggerMemberId = (Integer) triggerAssignee.get("id");

                                newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {


                                    Map<String, Object> task = (Map<String, Object>) action.getActionDetails().get("task");
                                    String taskName = (String) task.get("name");
                                    String description = (String) task.get("description");
                                    LocalDate dueDate = LocalDate.parse((String) task.get("dueDate"));
                                    List<Integer> collaboratorIds = (List<Integer>) task.get("collaboratorIds");
                                    List<Integer> teamIds = (List<Integer>) task.get("teamIds");
                                    String priority = (String) task.get("priority");

                                    System.out.println("Task: " + task);

                                    Map<String, Object> whichSection = (Map<String, Object>) action.getActionDetails().get("whichSection");
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
                            }


                            if (key.equals("Duedate is changed:Move task to section")) {
                                System.out.println("Duedate is changed ===> Move task to section");

                                Task task = modelMapper.map(taskRepository.findById(taskDTO.getTaskId()), Task.class);

                                if(!task.getDueDate().isEqual(taskDTO.getDueDate())) {

                                    Map<String, Object> ActionMovedSection = (Map<String, Object>) action.getActionDetails().get("ActionMovedSection");
                                    if (ActionMovedSection != null) {
                                        Integer workId = (Integer) ActionMovedSection.get("workId");
                                        taskDTO.setWorkId(workId);
                                    }
                                }
                            }

                            if (key.equals("Duedate is changed:Set task title")) {
                                System.out.println("Duedate is changed ===> Set task title");

                                Task task = modelMapper.map(taskRepository.findById(taskDTO.getTaskId()), Task.class);

                                if(!task.getDueDate().isEqual(taskDTO.getDueDate())) {

                                    String taskName = (String) action.getActionDetails().get("taskName");

                                    System.out.println("Due date changed ===> Set task title");
                                    System.out.println("taskDescription: " + taskName);
                                    if (taskName != null) {
                                        taskDTO.setTaskName(taskName);
                                    }
                                }
                            }

                            if (key.equals("Duedate is changed:Set task description")) {
                                System.out.println("Duedate is changed ===> Set task description");

                                Task task = modelMapper.map(taskRepository.findById(taskDTO.getTaskId()), Task.class);

                                if(!task.getDueDate().isEqual(taskDTO.getDueDate())) {

                                    String taskDescription = (String) action.getActionDetails().get("taskDescription");

                                    System.out.println("Due date changed ===> Set task description");
                                    System.out.println("taskDescription: " + taskDescription);
                                    if (taskDescription != null) {
                                        taskDTO.setDescription(taskDescription);
                                    }
                                }
                            }

                            if (key.equals("Duedate is changed:Complete task")) {
                                System.out.println("Duedate is changed ===> Complete task");

                                Task task = modelMapper.map(taskRepository.findById(taskDTO.getTaskId()), Task.class);

                                if(!task.getDueDate().isEqual(taskDTO.getDueDate())) {
                                    taskDTO.setStatus(true);
                                }
                            }

                            if (key.equals("Duedate is changed:Incomplete task")) {
                                System.out.println("Duedate is changed ===> Incomplete task");

                                Task task = modelMapper.map(taskRepository.findById(taskDTO.getTaskId()), Task.class);

                                if(!task.getDueDate().isEqual(taskDTO.getDueDate())) {
                                    taskDTO.setStatus(false);
                                }
                            }

                            if (key.equals("Duedate is changed:Create task")) {
                                System.out.println("Duedate is changed ===> Create task");

                                if(!existingTask.getDueDate().isEqual(taskDTO.getDueDate())) {
                                    Map<String, Object> task = (Map<String, Object>) action.getActionDetails().get("task");
                                    String taskName = (String) task.get("name");
                                    String description = (String) task.get("description");
                                    LocalDate dueDate = LocalDate.parse((String) task.get("dueDate"));
                                    List<Integer> collaboratorIds = (List<Integer>) task.get("collaboratorIds");
                                    List<Integer> teamIds = (List<Integer>) task.get("teamIds");
                                    String priority = (String) task.get("priority");
                                    Integer assignerId = (Integer) task.get("assignerId");

                                    System.out.println("Task: " + task);

                                    Map<String, Object> whichSection = (Map<String, Object>) action.getActionDetails().get("whichSection");
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
                                    newTaskDTO.setAssignerId(assignerId);
                                    newTaskDTO.setStatus(false);

                                    taskRepository.save(modelMapper.map(newTaskDTO, Task.class));
                                    taskProducer.sendCreateTaskMessage(newTaskDTO.getTaskName(), newTaskDTO.getAssignerId(), newTaskDTO.getCollaboratorIds());
                                }
                            }

                            if (key.equals("Duedate is changed:Set assignee to")) {
                                System.out.println("Duedate is changed ===> Set assignee to");

                                if(!existingTask.getDueDate().isEqual(taskDTO.getDueDate())) {
                                    Map<String, Object> triggerAssignee = (Map<String, Object>) trigger.getTriggerDetails().get("assignee");
                                    Integer triggerMemberId = (Integer) triggerAssignee.get("id");

                                    Map<String, Object> actionAssignee = (Map<String, Object>) action.getActionDetails().get("assignee");
                                    Integer actionMemberId = (Integer) actionAssignee.get("id");

                                    newCollaboratorIds.stream().filter(id -> id.equals(triggerMemberId)).findFirst().ifPresent(id -> {
                                        newCollaboratorIds.add(actionMemberId);
                                        taskDTO.setCollaboratorIds(newCollaboratorIds);
                                    });
                                }
                            }

                            if (key.equals("Duedate is changed:Clear assignee")) {
                                System.out.println("Duedate is changed ===> Clear assignee");

                                    if(!existingTask.getDueDate().isEqual(taskDTO.getDueDate())) {
                                        taskDTO.setCollaboratorIds(new ArrayList<>());
                                        taskDTO.setTeamIds(new ArrayList<>());
                                    }


                            }

                            if (key.equals("Duedate is changed:Remove task from the project")) {
                                System.out.println("Duedate is changed ===> Remove task from the project");

                                if(!existingTask.getDueDate().isEqual(taskDTO.getDueDate())) {
                                    taskRepository.deleteById(taskDTO.getTaskId());
                                }
                            }

                        }
                    }
                    catch (Exception e) {
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

//        Implemented new function
        List<PublishFlow> publishFlows = publishFlowRepository.findPublishFlowsByProjectId(taskDTO.getProjectId());

        System.out.println("publishFlows: " + publishFlows);
        if (publishFlows != null && !publishFlows.isEmpty()) {
            for (PublishFlow publishFlow : publishFlows) {
                if ("active".equals(publishFlow.getStatus())) {
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

                        System.out.println("triggers: " + triggers);
                        System.out.println("triggers size: " + triggers.size());

                        System.out.println("actions: " + actions);
                        System.out.println("Action size: " + actions.size());

                        for (int i = 0; i < actions.size(); i++) {
                            TriggerDTO trigger = triggers.get(0);
                            ActionDTO action = actions.get(i);

                            String triggerType = (String) trigger.getTriggerDetails().get("triggerType");
                            String actionType = (String) action.getActionDetails().get("actionType");
                            String key = triggerType + ":" + actionType;

                            System.out.println("Key: " + key);

                            // Handle each automation rule
                            if (key.equals("Section is:Set assignee to")) {
                                System.out.println("Section is ===> Set assignee to");
                                Map<String, Object> section = (Map<String, Object>) trigger.getTriggerDetails().get("section");
                                Integer triggerWorkId = (Integer) section.get("workId");

                                Object work = workWebClient.get()
                                        .uri("/getWork/{id}", taskDTO.getWorkId())
                                        .retrieve()
                                        .bodyToMono(new ParameterizedTypeReference<Object>() {})
                                        .block();

                                String workName = (String) ((Map<String, Object>) work).get("workName");
                                String triggerWorkName = (String) section.get("workName");

                                if (workName.equals(triggerWorkName)) {
                                    Map<String, Object> assignee = (Map<String, Object>) action.getActionDetails().get("assignee");
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
                            }

                            if (key.equals("Section changed:Move task to section")) {
                                System.out.println("Section changed ===> Move task to section");
                                Map<String, Object> actionMovedSection = (Map<String, Object>) action.getActionDetails().get("ActionMovedSection");
                                Integer movedWorkId = (Integer) actionMovedSection.get("workId");
                                taskDTO.setWorkId(movedWorkId);
                            }

                            if (key.equals("Section is:Move task to section")) {
                                System.out.println("Section is ===> Move task to section");
                                Map<String, Object> section = (Map<String, Object>) trigger.getTriggerDetails().get("section");
                                Integer triggerWorkId = (Integer) section.get("workId");
                                Map<String, Object> actionMovedSection = (Map<String, Object>) action.getActionDetails().get("ActionMovedSection");
                                Integer movedWorkId = (Integer) actionMovedSection.get("workId");

                                if (triggerWorkId.equals(taskDTO.getWorkId()) && !movedWorkId.equals(taskDTO.getWorkId())) {
                                    taskDTO.setWorkId(movedWorkId);
                                }
                            }

                            if (key.equals("Section changed:Set assignee to")) {
                                System.out.println("Section changed ===> Set assignee to");
                                Map<String, Object> assignee = (Map<String, Object>) action.getActionDetails().get("assignee");
                                Integer assigneeId = (Integer) assignee.get("id");
                                List<Integer> updatedCollaborators = taskDTO.getCollaboratorIds() != null
                                        ? new ArrayList<>(taskDTO.getCollaboratorIds())
                                        : new ArrayList<>();
                                if (!updatedCollaborators.contains(assigneeId)) {
                                    updatedCollaborators.add(assigneeId);
                                    taskDTO.setCollaboratorIds(updatedCollaborators);
                                }
                            }

                            if (key.equals("Section is:Clear assignee")) {
                                System.out.println("Section is ===> Clear assignee");
                                Map<String, Object> section = (Map<String, Object>) trigger.getTriggerDetails().get("section");
                                Integer triggerWorkId = (Integer) section.get("workId");

                                if (triggerWorkId.equals(taskDTO.getWorkId())) {
                                    taskDTO.setCollaboratorIds(new ArrayList<>());
                                    taskDTO.setTeamIds(new ArrayList<>());
                                }
                            }

                            if (key.equals("Section changed:Clear assignee")) {
                                System.out.println("Section changed ===> Clear assignee");
                                taskDTO.setCollaboratorIds(new ArrayList<>());
                                taskDTO.setTeamIds(new ArrayList<>());
                            }

                            if (key.equals("Section is:Remove task from the project")) {
                                System.out.println("Section is ===> Remove task from the project");
                                Map<String, Object> section = (Map<String, Object>) trigger.getTriggerDetails().get("section");
                                Integer triggerWorkId = (Integer) section.get("workId");

                                if (triggerWorkId != null && triggerWorkId.equals(taskDTO.getWorkId())) {
                                    Task taskEntity = taskRepository.findById(taskDTO.getTaskId())
                                            .orElseThrow(() -> new RuntimeException("Task not found"));
                                    taskRepository.deleteById(taskEntity.getTaskId());
                                    taskProducer.sendDeleteTaskMessage(taskEntity.getTaskName(), taskEntity.getAssignerId(), taskEntity.getCollaboratorIds());
                                    return "Task deleted successfully";
                                }
                            }

                            if (key.equals("Section changed:Remove task from the project")) {
                                System.out.println("Section changed ===> Remove task from the project");
                                Task task = taskRepository.findById(taskDTO.getTaskId())
                                        .orElseThrow(() -> new RuntimeException("Task not found"));
                                if (!Objects.equals(task.getWorkId(), taskDTO.getWorkId())) {
                                    taskRepository.deleteById(task.getTaskId());
                                    taskProducer.sendDeleteTaskMessage(task.getTaskName(), task.getAssignerId(), task.getCollaboratorIds());
                                    return "Task deleted successfully";
                                }
                            }

                            if (key.equals("Section changed:Complete task") || key.equals("Section is:Complete task")) {

                                if (key.startsWith("Section is")) {
                                    System.out.println("Section is ===> Complete task");
                                    Map<String, Object> section = (Map<String, Object>) trigger.getTriggerDetails().get("section");
                                    Integer triggerWorkId = (Integer) section.get("workId");

                                    if (triggerWorkId.equals(taskDTO.getWorkId())) {
                                        taskDTO.setStatus(true);
                                    }
                                } else {
                                    System.out.println("Section changed ===> Complete task");
                                    taskDTO.setStatus(true);
                                }
                            }

                            if (key.equals("Section changed:Incomplete task") || key.equals("Section is:Incomplete task")) {
                                if (key.startsWith("Section is")) {
                                    System.out.println("Section is ===> Incomplete task");
                                    Map<String, Object> section = (Map<String, Object>) trigger.getTriggerDetails().get("section");
                                    Integer triggerWorkId = (Integer) section.get("workId");

                                    if (triggerWorkId.equals(taskDTO.getWorkId())) {
                                        taskDTO.setStatus(false);
                                    }
                                } else {
                                    System.out.println("Section changed ===> Incomplete task");
                                    taskDTO.setStatus(false);
                                }
                            }

                            if (key.equals("Section changed:Set task title") || key.equals("Section is:Set task title")) {
                                String taskName = (String) action.getActionDetails().get("taskName");

                                if (key.startsWith("Section is")) {
                                    System.out.println("Section is ===> Set task title");
                                    Map<String, Object> section = (Map<String, Object>) trigger.getTriggerDetails().get("section");
                                    Integer triggerWorkId = (Integer) section.get("workId");

                                    if (triggerWorkId.equals(taskDTO.getWorkId())) {
                                        taskDTO.setTaskName(taskName);
                                    }
                                } else {
                                    System.out.println("Section changed ===> Set task title");
                                    taskDTO.setTaskName(taskName);
                                }
                            }

                            if (key.equals("Section changed:Set task description") || key.equals("Section is:Set task description")) {
                                String taskDescription = (String) action.getActionDetails().get("taskDescription");

                                if (key.startsWith("Section is")) {
                                    System.out.println("Section is ===> Set task description");
                                    Map<String, Object> section = (Map<String, Object>) trigger.getTriggerDetails().get("section");
                                    Integer triggerWorkId = (Integer) section.get("workId");

                                    if (triggerWorkId.equals(taskDTO.getWorkId())) {
                                        taskDTO.setDescription(taskDescription);
                                    }
                                } else {
                                    System.out.println("Section changed ===> Set task description");
                                    taskDTO.setDescription(taskDescription);
                                }
                            }

                            if (key.equals("Section changed:Set due date to") || key.equals("Section is:Set due date to")) {
                                String dateStr = (String) action.getActionDetails().get("date");
                                LocalDate date = LocalDate.parse(dateStr);

                                if (key.startsWith("Section is")) {
                                    System.out.println("Section is ===> Set due date to");
                                    Map<String, Object> section = (Map<String, Object>) trigger.getTriggerDetails().get("section");
                                    Integer triggerWorkId = (Integer) section.get("workId");

                                    if (triggerWorkId.equals(taskDTO.getWorkId())) {
                                        taskDTO.setDueDate(date);
                                    }
                                } else {
                                    System.out.println("Section changed ===> Set due date to");
                                    taskDTO.setDueDate(date);
                                }
                            }

                            if (key.equals("Section changed:Create task") || key.equals("Section is:Create task")) {
                                Map<String, Object> newTask = (Map<String, Object>) action.getActionDetails().get("task");
                                String taskName = (String) newTask.get("name");
                                String description = (String) newTask.get("description");
                                LocalDate dueDate = LocalDate.parse((String) newTask.get("dueDate"));
                                List<Integer> collaboratorIds = (List<Integer>) newTask.get("collaboratorIds");
                                List<Integer> teamIds = (List<Integer>) newTask.get("teamIds");
                                String priority = (String) newTask.get("priority");

                                Map<String, Object> whichSection = (Map<String, Object>) action.getActionDetails().get("whichSection");
                                Integer actionWorkId = (Integer) whichSection.get("workId");

                                boolean shouldCreate = key.startsWith("Section changed");
                                if (key.startsWith("Section is")) {
                                    System.out.println("Section is ===> Create task");
                                    Map<String, Object> section = (Map<String, Object>) trigger.getTriggerDetails().get("section");
                                    Integer triggerWorkId = (Integer) section.get("workId");
                                    shouldCreate = triggerWorkId.equals(taskDTO.getWorkId());
                                }

                                if (shouldCreate) {
                                    System.out.println("Section changed ===> Create task");
                                    TaskDTO newTaskDTO = new TaskDTO();
                                    newTaskDTO.setTaskName(taskName);
                                    newTaskDTO.setDescription(description);
                                    newTaskDTO.setDueDate(dueDate);
                                    newTaskDTO.setCollaboratorIds(collaboratorIds);
                                    newTaskDTO.setTeamIds(teamIds);
                                    newTaskDTO.setTags(taskDTO.getTags());
                                    newTaskDTO.setPriority(TaskPriorityLevel.valueOf(priority.toUpperCase()));
                                    newTaskDTO.setProjectId(taskDTO.getProjectId());
                                    newTaskDTO.setWorkId(actionWorkId);
                                    newTaskDTO.setAssignerId(taskDTO.getAssignerId());
                                    newTaskDTO.setStatus(false);

                                    taskRepository.save(modelMapper.map(newTaskDTO, Task.class));
                                    taskProducer.sendCreateTaskMessage(newTaskDTO.getTaskName(), newTaskDTO.getAssignerId(), newTaskDTO.getCollaboratorIds());
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

        Optional<Task> taskOptional = taskRepository.findById(id);
        if (taskOptional.isPresent()) {
            TaskDTO task = modelMapper.map(taskOptional.get(), TaskDTO.class);
            taskRepository.deleteById(id);

            taskProducer.sendDeleteTaskMessage(task.getTaskName(), task.getAssignerId(), task.getCollaboratorIds());
            return "Successfully deleted";
        } else {
            return null;
        }
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

        System.out.println("publishFlows: " + publishFlows);
        if (publishFlows != null && !publishFlows.isEmpty()) {
            for (PublishFlow publishFlow : publishFlows) {
                if ("active".equals(publishFlow.getStatus())) {
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

                        System.out.println("triggers: " + triggers);
                        System.out.println("triggers size: " + triggers.size());

                        System.out.println("actions: " + actions);
                        System.out.println("Action size: " + actions.size());

                        for (int i = 0; i < actions.size(); i++) {
                            TriggerDTO trigger = triggers.get(0);
                            ActionDTO action = actions.get(i);

                            String triggerType = (String) trigger.getTriggerDetails().get("triggerType");
                            String actionType = (String) action.getActionDetails().get("actionType");
                            String key = triggerType + ":" + actionType;

                            System.out.println("Key: " + key);

                            if (key.equals("Status is changed:Move task to section")) {
                                System.out.println("Status is changed ===> Move task to section");
                                if(task.isStatus()) {
                                    Map<String, Object> ActionMovedSection = (Map<String, Object>) action.getActionDetails().get("ActionMovedSection");
                                    if (ActionMovedSection != null) {
                                        Integer workId = (Integer) ActionMovedSection.get("workId");
                                        task.setWorkId(workId);
                                    }
                                }

                                else {
                                    Map<String, Object> ActionMovedSection = (Map<String, Object>) action.getActionDetails().get("ActionMovedSection");
                                    if (ActionMovedSection != null) {
                                        Integer workId = (Integer) ActionMovedSection.get("workId");
                                        task.setWorkId(workId);
                                    }
                                }
                            }

                            if (key.equals("Status is incomplete:Move task to section")) {
                                if(task.isStatus()) {
                                    Map<String, Object> ActionMovedSection = (Map<String, Object>) action.getActionDetails().get("ActionMovedSection");
                                    if (ActionMovedSection != null) {
                                        Integer workId = (Integer) ActionMovedSection.get("workId");
                                        task.setWorkId(workId);
                                    }
                                }
                            }

                            if (key.equals("Status is complete:Move task to section")) {
                                if(!task.isStatus()) {
                                    Map<String, Object> ActionMovedSection = (Map<String, Object>) action.getActionDetails().get("ActionMovedSection");
                                    if (ActionMovedSection != null) {
                                        Integer workId = (Integer) ActionMovedSection.get("workId");
                                        task.setWorkId(workId);
                                    }
                                }
                            }

                            if (key.equals("Status is changed:Create task")) {
                                System.out.println("Status is changed ===> Create task");
                                    Map<String, Object> taskNew = (Map<String, Object>) action.getActionDetails().get("task");
                                    String taskName = (String) taskNew.get("name");
                                    String description = (String) taskNew.get("description");
                                    LocalDate dueDate = LocalDate.parse((String) taskNew.get("dueDate"));
                                    List<Integer> collaboratorIds = (List<Integer>) taskNew.get("collaboratorIds");
                                    List<Integer> teamIds = (List<Integer>) taskNew.get("teamIds");
                                    Integer assignerId = (Integer) task.getAssignerId();
                                    String priority  = (String) taskNew.get("priority");
                                    List<String> tags = (List<String>) task.getTags();

                                    System.out.println("Task: " + task);

                                    Map<String, Object> whichSection = (Map<String, Object>) action.getActionDetails().get("whichSection");
                                    Integer ActionWorkId = (Integer) whichSection.get("workId");

                                    TaskDTO newTaskDTO = new TaskDTO();

                                    newTaskDTO.setTaskName(taskName);
                                    newTaskDTO.setDescription(description);
                                    newTaskDTO.setDueDate(dueDate);
                                    newTaskDTO.setCollaboratorIds(collaboratorIds);
                                    newTaskDTO.setTeamIds(teamIds);
                                    newTaskDTO.setTags(tags);
                                    newTaskDTO.setPriority(TaskPriorityLevel.valueOf(priority.toUpperCase()));
                                    newTaskDTO.setProjectId(task.getProjectId());
                                    newTaskDTO.setWorkId(ActionWorkId);
                                    newTaskDTO.setAssignerId(assignerId);
                                    newTaskDTO.setStatus(false);

                                    taskRepository.save(modelMapper.map(newTaskDTO, Task.class));
                                    taskProducer.sendCreateTaskMessage(newTaskDTO.getTaskName(), newTaskDTO.getAssignerId(), newTaskDTO.getCollaboratorIds());
                            }

                            if (key.equals("Status is incomplete:Create task")) {

                                System.out.println("Status is incomplete ===> Create task");
                                if(task.isStatus()) {
                                    Map<String, Object> taskNew = (Map<String, Object>) action.getActionDetails().get("task");
                                    String taskName = (String) taskNew.get("name");
                                    String description = (String) taskNew.get("description");
                                    LocalDate dueDate = LocalDate.parse((String) taskNew.get("dueDate"));
                                    List<Integer> collaboratorIds = (List<Integer>) taskNew.get("collaboratorIds");
                                    List<Integer> teamIds = (List<Integer>) taskNew.get("teamIds");
                                    String priority  = (String) taskNew.get("priority");
                                    Integer assignerId = (Integer) task.getAssignerId();
                                    List<String> tags = (List<String>) task.getTags();

                                    System.out.println("Task: " + task);

                                    Map<String, Object> whichSection = (Map<String, Object>) action.getActionDetails().get("whichSection");
                                    Integer ActionWorkId = (Integer) whichSection.get("workId");

                                    TaskDTO newTaskDTO = new TaskDTO();

                                    newTaskDTO.setTaskName(taskName);
                                    newTaskDTO.setDescription(description);
                                    newTaskDTO.setDueDate(dueDate);
                                    newTaskDTO.setCollaboratorIds(collaboratorIds);
                                    newTaskDTO.setTeamIds(teamIds);
                                    newTaskDTO.setTags(tags);
                                    newTaskDTO.setPriority(TaskPriorityLevel.valueOf(priority.toUpperCase()));
                                    newTaskDTO.setProjectId(task.getProjectId());
                                    newTaskDTO.setWorkId(ActionWorkId);
                                    newTaskDTO.setAssignerId(assignerId);
                                    newTaskDTO.setStatus(false);

                                    taskRepository.save(modelMapper.map(newTaskDTO, Task.class));
                                    taskProducer.sendCreateTaskMessage(newTaskDTO.getTaskName(), newTaskDTO.getAssignerId(), newTaskDTO.getCollaboratorIds());
                                }
                            }

                            if (key.equals("Status is complete:Create task")) {

                                System.out.println("Status is complete ===> Create task");
                                if(!task.isStatus()) {
                                    Map<String, Object> taskNew = (Map<String, Object>) action.getActionDetails().get("task");
                                    String taskName = (String) taskNew.get("name");
                                    String description = (String) taskNew.get("description");
                                    LocalDate dueDate = LocalDate.parse((String) taskNew.get("dueDate"));
                                    List<Integer> collaboratorIds = (List<Integer>) taskNew.get("collaboratorIds");
                                    List<Integer> teamIds = (List<Integer>) taskNew.get("teamIds");
                                    String priority  = (String) taskNew.get("priority");
                                    Integer assignerId = (Integer) task.getAssignerId();
                                    List<String> tags = (List<String>) task.getTags();

                                    System.out.println("Task: " + task);

                                    Map<String, Object> whichSection = (Map<String, Object>) action.getActionDetails().get("whichSection");
                                    Integer ActionWorkId = (Integer) whichSection.get("workId");

                                    TaskDTO newTaskDTO = new TaskDTO();

                                    newTaskDTO.setTaskName(taskName);
                                    newTaskDTO.setDescription(description);
                                    newTaskDTO.setDueDate(dueDate);
                                    newTaskDTO.setCollaboratorIds(collaboratorIds);
                                    newTaskDTO.setTeamIds(teamIds);
                                    newTaskDTO.setTags(tags);
                                    newTaskDTO.setPriority(TaskPriorityLevel.valueOf(priority.toUpperCase()));
                                    newTaskDTO.setProjectId(task.getProjectId());
                                    newTaskDTO.setWorkId(ActionWorkId);
                                    newTaskDTO.setAssignerId(assignerId);
                                    newTaskDTO.setStatus(false);

                                    taskRepository.save(modelMapper.map(newTaskDTO, Task.class));
                                    taskProducer.sendCreateTaskMessage(newTaskDTO.getTaskName(), newTaskDTO.getAssignerId(), newTaskDTO.getCollaboratorIds());
                                }
                            }


                        }
                    }
                    catch (Exception e) {
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
        taskProducer.sendUpdateStatusTaskMessage(task.getTaskName(), task.getAssignerId(), "Status changed", task.getCollaboratorIds());
    }


    //    Task Template methods
    @Override
    public String createTaskTemplate(TemplateDTO templateDTO) {
        templateRepository.save(modelMapper.map(templateDTO, Template.class));
        return "Task template created successfully";
    }

    @Override
    public TemplateDTO getTaskTemplateById(int id) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        return modelMapper.map(template, TemplateDTO.class);
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
    public String deleteTaskTemplate(int id) {
        templateRepository.deleteById(id);
        return "Task deleted successfully";
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
    public String deleteRule(int id) {
        ruleRepository.deleteById(id);
        return "Rule deleted successfully";

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
            // Save as a new record
            PublishFlow newFlow = modelMapper.map(publishFlowDTO, PublishFlow.class);
            newFlow.setCreatedAt(LocalDateTime.now());
            newFlow.setUpdatedAt(LocalDateTime.now());
            publishFlowRepository.save(newFlow);

            return "Publish flow created successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error while creating or updating publish flow";
        }
    }

    @Override
    public PublishFlowDTO getPublishFlowById(int id) {
        PublishFlow publishFlow = publishFlowRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("PublishFlow not found with id: " + id));

        return modelMapper.map(publishFlow, PublishFlowDTO.class);
    }

    @Override
    public String updatePublishFlow(PublishFlowDTO publishFlowDTO) {
        publishFlowRepository.save(modelMapper.map(publishFlowDTO, new TypeToken<PublishFlow>(){}.getType()));
        return "Publish flow updated successfully";
    }

    @Override
    public String deletePublishFlow(int id) {
        publishFlowRepository.deleteById(id);
        return "Publish flow deleted successfully";
    }

    @Override
    public PublishFlowDTO findPublishFlowByProjectId(int projectId) {
        List<PublishFlow> publishFlow = publishFlowRepository.findPublishFlowsByProjectId(projectId);
        if (publishFlow == null) {
            return null;
        }
//        return modelMapper.map(publishFlow, PublishFlowDTO.class);
        return modelMapper.map(publishFlow.get(0), PublishFlowDTO.class);
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
