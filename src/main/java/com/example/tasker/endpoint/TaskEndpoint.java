package com.example.tasker.endpoint;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tasker.exception.CustomException;
import com.example.tasker.model.dto.TaskDTO;
import com.example.tasker.model.dto.TaskSearchCriteria;
import com.example.tasker.service.TaskService;

@RestController
@RequestMapping("api/tasks")
public class TaskEndpoint {

    private final TaskService taskService;

    public TaskEndpoint(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping(value = "/{id}")
    public TaskDTO getTaskById(@PathVariable("id") Long id) throws CustomException {
        return taskService.getTaskDTOById(id);
    }

    @GetMapping
    public List<TaskDTO> getTasksBySearchCriteria(@RequestBody TaskSearchCriteria taskSearchCriteria) throws CustomException {
        return taskService.getTasksBySearchCriteria(taskSearchCriteria);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_TASK')")
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO taskDTO) throws CustomException {
        TaskDTO createdTask = taskService.createTask(taskDTO);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('CREATE_TASK')")
    public ResponseEntity<TaskDTO> updateTask(@RequestBody TaskDTO taskDTO) throws CustomException {
        TaskDTO updatedTask = taskService.updateTask(taskDTO);
        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_TASK')")
    public ResponseEntity<Object> deleteTask(@PathVariable("id") Long id) throws CustomException {
        taskService.deleteTask(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
