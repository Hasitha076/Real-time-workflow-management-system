package edu.IIT.project_management.service;

import edu.IIT.project_management.dto.CollaboratorsRequest;
import edu.IIT.project_management.dto.ProjectDTO;
import edu.IIT.project_management.model.Project;
import edu.IIT.project_management.producer.ProjectProducer;
import edu.IIT.project_management.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ModelMapper modelMapper;
    private final ProjectProducer projectProducer;
    private final WebClient userWebClient;
    private final WebClient teamWebClient;

    @Override
    public String createProject(ProjectDTO projectDTO) {

        List<String> users = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUserNames") // Ensure path matches the controller
                        .queryParam("ids", projectDTO.getCollaboratorIds()) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        List<String> teams = teamWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterTeams") // Ensure path matches the controller
                        .queryParam("ids", projectDTO.getTeamIds()) // Ensure param name matches the controller
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
        projectDTO.setMemberIcons(memberIcons);


        projectRepository.save(modelMapper.map(projectDTO, Project.class));
        projectProducer.sendCreatedProjectMessage(projectDTO.getProjectName(), projectDTO.getAssignerId(), projectDTO.getCollaboratorIds());
        return "Project created successfully";
    }

    @Override
    public ProjectDTO getProjectById(int id) {
        return modelMapper.map(projectRepository.findById(id), ProjectDTO.class);
    }

    @Override
    public void updateCollaborators(int projectId, CollaboratorsRequest collaboratorsRequest) {
        // Retrieve the existing project
        ProjectDTO project = getProjectById(projectId);

        // Get the current collaborators and teams
        List<Integer> existingCollaborators = project.getCollaboratorIds();
        List<Integer> existingTeams = project.getTeamIds();

        // If the lists are null, initialize them
        if (existingCollaborators == null) {
            existingCollaborators = new ArrayList<>();
        }
        if (existingTeams == null) {
            existingTeams = new ArrayList<>();
        }

        // Add new collaborators and teams (avoid duplicates)
        for (Integer collaboratorId : collaboratorsRequest.getCollaboratorIds()) {
            if (!existingCollaborators.contains(collaboratorId)) {
                existingCollaborators.add(collaboratorId);
            }
        }
        for (Integer teamId : collaboratorsRequest.getTeamIds()) {
            if (!existingTeams.contains(teamId)) {
                existingTeams.add(teamId);
            }
        }

        // Update the project with the new lists
        project.setCollaboratorIds(collaboratorsRequest.getCollaboratorIds());
        project.setTeamIds(collaboratorsRequest.getTeamIds());

        // Save the updated project
        updateProject(project);
    }


    @Override
    public String updateProject(ProjectDTO projectDTO) {
        System.out.println("ProjectDTO: " + projectDTO);
        Optional<Project> project = projectRepository.findById(projectDTO.getProjectId());
        if (project.isEmpty()) {
            return "Project not found";
        }

        System.out.println("Searched Project: " + project);

        // Retain the createdAt value from the old project
        projectDTO.setCreatedAt(project.get().getCreatedAt());
        List<Integer> oldCollaboratorIds = project.get().getCollaboratorIds(); // Get existing collaborators

        // Identify new collaborators: present in new project but not in old project
        List<Integer> newCollaborators = projectDTO.getCollaboratorIds().stream()
                .filter(collaboratorId -> !oldCollaboratorIds.contains(collaboratorId))
                .collect(Collectors.toList());

        // Identify removed collaborators: present in old project but not in new project
        List<Integer> removedCollaborators = oldCollaboratorIds.stream()
                .filter(collaboratorId -> !projectDTO.getCollaboratorIds().contains(collaboratorId))
                .collect(Collectors.toList());

        // Identify unchanged collaborators: present in both old and new project
        List<Integer> unchangedCollaborators = oldCollaboratorIds.stream()
                .filter(projectDTO.getCollaboratorIds()::contains)
                .collect(Collectors.toList());

        List<String> users = userWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterUserNames") // Ensure path matches the controller
                        .queryParam("ids", projectDTO.getCollaboratorIds()) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        List<String> teams = teamWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/filterTeams") // Ensure path matches the controller
                        .queryParam("ids", projectDTO.getTeamIds()) // Ensure param name matches the controller
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        System.out.println("Updated Users: " + users);
        System.out.println("Updated Teams: " + teams);

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
        projectDTO.setMemberIcons(memberIcons);

        System.out.println("New ProjectDTO: " + projectDTO);

        // Perform the update in the repository
        projectRepository.save(modelMapper.map(projectDTO, new TypeToken<Project>(){}.getType()));

        // Handle sending collaborator changes to the producer or further actions
        if (!newCollaborators.isEmpty()) {
            // Send newly added collaborators
            projectProducer.sendUpdateProjectMessage(projectDTO.getProjectName(), projectDTO.getAssignerId(),"New", newCollaborators);
        }

        if (!removedCollaborators.isEmpty()) {
            // Send removed collaborators
            projectProducer.sendUpdateProjectMessage(projectDTO.getProjectName(), projectDTO.getAssignerId(),"Removed", removedCollaborators);
        }

        // Optionally, you can also send unchanged collaborators if needed
        if (!unchangedCollaborators.isEmpty()) {
            // Handle unchanged collaborators if necessary
            projectProducer.sendUpdateProjectMessage(projectDTO.getProjectName(), projectDTO.getAssignerId(),"Existing", unchangedCollaborators);
        }

        return "Project updated successfully";
    }


    @Override
    public void deleteProject(int id) {
        ProjectDTO project = getProjectById(id);
        projectRepository.deleteById(id);
        projectProducer.sendDeleteProjectMessage(project.getProjectId(), project.getProjectName(), project.getAssignerId(), project.getCollaboratorIds());
    }

    @Override
    public List<ProjectDTO> getAllProjects() {
        return modelMapper.map(projectRepository.findAll(), new TypeToken<List<ProjectDTO>>(){}.getType());
    }

    @Override
    public List<ProjectDTO> getProjectsByTeamId(int teamId) {
        List<Project> projects = projectRepository.findByTeamIds(teamId);

        log.info("Projects: {}", projects);

        if (projects.isEmpty()) {
            throw new ResourceNotFoundException("No projects found for team ID: " + teamId);
        }

        return modelMapper.map(projects, new TypeToken<List<ProjectDTO>>(){}.getType());
    }

}
