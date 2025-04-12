package edu.IIT.task_management.service;

import edu.IIT.task_management.dto.*;

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

    public String createTaskTemplate(TemplateDTO taskTemplateDTO);
    public TemplateDTO getTaskTemplateById(int id);
    public String updateTaskTemplate(TemplateDTO taskTemplateDTO);
    public void deleteTaskTemplate(int id);
    public List<TemplateDTO> getAllTaskTemplates();
    public List<TemplateDTO> getTaskTemplatesByProjectId(int projectId);

    public void createCollaboratorsBlock(CollaboratorsBlockDTO collaboratorsBlockDTO);
    public String updateCollaboratorsBlock(CollaboratorsBlockDTO collaboratorsBlockDTO);
    public CollaboratorsBlockDTO getCollaboratorsBlockByWorkId(int workId);

    public String createRule(RuleDTO ruleDTO);
    public RuleDTO getRuleById(int id);
    public String updateRule(RuleDTO ruleDTO);
    public void deleteRule(int id);
    public List<RuleDTO> getAllRules();
    public List<RuleDTO> getRulesByProjectId(int projectId);

    public String createPublishFlow(PublishFlowDTO publishFlowDTO);
    public PublishFlowDTO getPublishFlowById(int id);
    public String updatePublishFlow(PublishFlowDTO publishFlowDTO);
    public void deletePublishFlow(int id);
    public PublishFlowDTO findPublishFlowByProjectId(int projectId);
}
