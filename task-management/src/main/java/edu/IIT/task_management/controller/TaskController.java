package edu.IIT.task_management.controller;

import edu.IIT.task_management.dto.*;
import edu.IIT.task_management.producer.TaskProducer;
import edu.IIT.task_management.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
@CrossOrigin
public class TaskController {

    private final TaskService taskService;
    private final TaskProducer taskProducer;

    @PostMapping("/createTask")
    public String addTask(@RequestBody TaskDTO taskDTO) {
    taskProducer.sendMessage(taskDTO);
        return taskService.createTask(taskDTO);
    }

    @PutMapping("/updateTask")
    public String updateTask(@RequestBody TaskDTO taskDTO) {

        return taskService.updateTask(taskDTO);
    }

    @DeleteMapping("/deleteTask/{id}")
    public String deleteTask(@PathVariable int id) {
        taskService.deleteTask(id);
        return "Task deleted successfully";
    }

    @GetMapping("/getTask/{id}")
    public TaskDTO getTask(@PathVariable int id) {
//        taskProducer.sendMessage(taskService.getTaskById(id));
        return taskService.getTaskById(id);
    }

    @GetMapping("/getTasksByProjectId/{id}")
    public List<TaskDTO> getTasksByProjectId(@PathVariable int id) {
        return taskService.getTasksByProjectId(id);
    }

    @GetMapping("/getTasksByWorkId/{id}")
    public List<TaskDTO> getTasksByWorkId(@PathVariable int id) {
        return taskService.getTasksByWorkId(id);
    }

    @GetMapping("/getAllTasks")
    public List<TaskDTO> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PutMapping("/changeTaskStatus/{taskId}")
    public void changeTaskStatus(@PathVariable int taskId) {
        taskService.changeTaskStatus(taskId);
    }

    @PostMapping("/createTaskTemplate")
    public String addTaskTemplate(@RequestBody TemplateDTO taskTemplateDTO) {
        return taskService.createTaskTemplate(taskTemplateDTO);
    }

    @PutMapping("/updateTaskTemplate")
    public String updateTaskTemplate(@RequestBody TemplateDTO taskTemplateDTO) {
        System.out.println("Task template update: " + taskTemplateDTO);
        return taskService.updateTaskTemplate(taskTemplateDTO);
    }

    @DeleteMapping("/deleteTaskTemplate/{id}")
    public String deleteTaskTemplate(@PathVariable int id) {
        taskService.deleteTaskTemplate(id);
        return "Task deleted successfully";
    }

    @GetMapping("/getTaskTemplate/{id}")
    public TemplateDTO getTaskTemplate(@PathVariable int id) {
        return taskService.getTaskTemplateById(id);
    }

    @GetMapping("/getAllTaskTemplates")
    public List<TemplateDTO> getAllTaskTemplates() {
        return taskService.getAllTaskTemplates();
    }

    @GetMapping("/getTaskTemplatesByProjectId/{projectId}")
    public List<TemplateDTO> getTaskTemplateByProjectId(@PathVariable int projectId) {
        return taskService.getTaskTemplatesByProjectId(projectId);
    }

    @PostMapping("/createCollaboratorsBlock")
    public void createCollaboratorsBlock(@RequestBody CollaboratorsBlockDTO collaboratorsBlockDTO) {
        taskService.createCollaboratorsBlock(collaboratorsBlockDTO);
    }

    @PutMapping("/updateCollaboratorsBlock")
    public String updateCollaboratorsBlock(@RequestBody CollaboratorsBlockDTO collaboratorsBlockDTO) {
        return taskService.updateCollaboratorsBlock(collaboratorsBlockDTO);
    }

    @GetMapping("/getCollaboratorsBlock/{workId}")
    public CollaboratorsBlockDTO getCollaboratorsBlock(@PathVariable int workId) {
        return taskService.getCollaboratorsBlockByWorkId(workId);
    }

    @PostMapping("/createRule")
    public String createRule(@RequestBody RuleDTO ruleDTO) {
        return taskService.createRule(ruleDTO);
    }

    @PutMapping("/updateRule")
    public String updateRule(@RequestBody RuleDTO ruleDTO) {
        return taskService.updateRule(ruleDTO);
    }

    @DeleteMapping("/deleteRule/{id}")
    public String deleteRule(@PathVariable int id) {
        taskService.deleteRule(id);
        return "Rule deleted successfully";
    }

    @GetMapping("/getRule/{id}")
    public RuleDTO getRule(@PathVariable int id) {
        return taskService.getRuleById(id);
    }

    @GetMapping("/getAllRules")
    public List<RuleDTO> getAllRules() {
        return taskService.getAllRules();
    }

    @GetMapping("/getRulesByProjectId/{projectId}")
    public List<RuleDTO> getRulesByProjectId(@PathVariable int projectId) {
        return taskService.getRulesByProjectId(projectId);
    }

    @PostMapping("/createPublishFlow")
    public String createPublishFlow(@RequestBody PublishFlowDTO publishFlowDTO) {
        return taskService.createPublishFlow(publishFlowDTO);
    }

    @PutMapping("/updatePublishFlow")
    public String updatePublishFlow(@RequestBody PublishFlowDTO publishFlowDTO) {
        return taskService.updatePublishFlow(publishFlowDTO);
    }

    @DeleteMapping("/deletePublishFlow/{id}")
    public String deletePublishFlow(@PathVariable int id) {
        taskService.deletePublishFlow(id);
        return "Publish flow deleted successfully";
    }

    @GetMapping("/getPublishFlow/{id}")
    public PublishFlowDTO getPublishFlow(@PathVariable int id) {
        return taskService.getPublishFlowById(id);
    }

    @GetMapping("/getPublishFlowByProjectId/{projectId}")
    public PublishFlowDTO getPublishFlowByProjectId(@PathVariable int projectId) {
        return taskService.findPublishFlowByProjectId(projectId);
    }

    @PutMapping("/movedAndUpdateTask")
    public String movedAndUpdateTask(@RequestBody TaskDTO taskDTO) {
        return taskService.movedAndUpdateTask(taskDTO);
    }

    @GetMapping("/getPublishFlowByRuleId/{ruleId}")
    public PublishFlowDTO getPublishFlowByRuleId(@PathVariable int ruleId) {
        return taskService.findPublishFlowByRuleId(ruleId);
    }

    @DeleteMapping("/deletePublishFlowByRuleId/{ruleId}")
    public void deletePublishFlowByRuleId(@PathVariable int ruleId) {
        taskService.deletePublishFlowByRuleId(ruleId);
    }


}
