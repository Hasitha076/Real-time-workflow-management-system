package edu.IIT.work_management.controller;

import edu.IIT.project_management.dto.CollaboratorsRequest;
import edu.IIT.work_management.dto.WorkDTO;
import edu.IIT.work_management.dto.WorkStatusUpdateRequest;
import edu.IIT.work_management.producer.WorkProducer;
import edu.IIT.work_management.service.WorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/work")
@RequiredArgsConstructor
@CrossOrigin
public class WorkController {

    private final WorkService workService;
    private final WorkProducer workProducer;

    @PostMapping("/createWork")
    public String addWork(@RequestBody WorkDTO workDTO) {
    workProducer.sendMessage(workDTO);
        return workService.createWork(workDTO);
    }

    @PutMapping("/updateWork")
    public String updateWork(@RequestBody WorkDTO workDTO) {
        return workService.updateWork(workDTO);
    }

    @DeleteMapping("/deleteWork/{id}")
    public String deleteTask(@PathVariable int id) {
        workService.deleteWork(id);
        return "Work deleted successfully";
    }

    @GetMapping("/getWork/{id}")
    public WorkDTO getWork(@PathVariable int id) {
//        taskProducer.sendMessage(taskService.getTaskById(id));
        return workService.getWorkById(id);
    }

    @GetMapping("/getWorksByProjectId/{id}")
    public List<WorkDTO> getWorksByProjectId(@PathVariable int id) {
        return workService.getWorksByProjectId(id);
    }

    @GetMapping("/getAllWorks")
    public List<WorkDTO> getAllTasks() {
        return workService.getAllWorks();
    }

    @PutMapping("/updateCollaborators/{workId}")
    public void updateCollaborators(@PathVariable int workId, @RequestBody CollaboratorsRequest collaboratorsRequest) {
        workService.updateCollaborators(workId, collaboratorsRequest);
    }

    @GetMapping("/getWorksByTeamId/{teamId}")
    public List<WorkDTO> getWorksByTeamId(@PathVariable int teamId) {
        return workService.getWorksByTeamId(teamId);
    }

    @PutMapping("/updateWorkStatus")
    public ResponseEntity<String> updateWorkStatus(@RequestBody WorkStatusUpdateRequest request) {
        return ResponseEntity.ok(workService.updateWorkStatus(request.getWorkId()));
    }

}
