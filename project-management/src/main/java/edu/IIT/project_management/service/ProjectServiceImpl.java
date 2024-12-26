package edu.IIT.project_management.service;

import edu.IIT.project_management.dto.ProjectDTO;
import edu.IIT.project_management.model.Project;
import edu.IIT.project_management.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ModelMapper modelMapper;

    @Override
    public String createProject(ProjectDTO projectDTO) {
        projectRepository.save(modelMapper.map(projectDTO, Project.class));
        return "Project created successfully";
    }

    @Override
    public ProjectDTO getProjectById(int id) {
        return modelMapper.map(projectRepository.findById(id), ProjectDTO.class);
    }

    @Override
    public String updateProject(ProjectDTO projectDTO) {
        Optional<Project> project = (projectRepository.findById(projectDTO.getProjectId()));
        if (project.isEmpty()) {
            return "Project not found";
        }
        projectDTO.setCreatedAt(project.get().getCreatedAt());
        projectRepository.save(modelMapper.map(projectDTO, new TypeToken<Project>(){}.getType()));
        return "Project updated successfully";
    }

    @Override
    public void deleteProject(int id) {
        projectRepository.deleteById(id);
    }

    @Override
    public List<ProjectDTO> getAllProjects() {
        return modelMapper.map(projectRepository.findAll(), new TypeToken<List<ProjectDTO>>(){}.getType());
    }

}
