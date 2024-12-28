package edu.IIT.project_management.service;

import edu.IIT.project_management.dto.ProjectDTO;
import edu.IIT.project_management.model.Project;
import edu.IIT.project_management.producer.ProjectProducer;
import edu.IIT.project_management.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

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

    @Override
    public String createProject(ProjectDTO projectDTO) {
        projectRepository.save(modelMapper.map(projectDTO, Project.class));
        projectProducer.sendCreatedProjectMessage(projectDTO.getProjectName(), projectDTO.getAssignerId(), projectDTO.getCollaboratorIds());
        return "Project created successfully";
    }

    @Override
    public ProjectDTO getProjectById(int id) {
        return modelMapper.map(projectRepository.findById(id), ProjectDTO.class);
    }

    @Override
    public String updateProject(ProjectDTO projectDTO) {
        Optional<Project> project = projectRepository.findById(projectDTO.getProjectId());
        if (project.isEmpty()) {
            return "Project not found";
        }

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

}
