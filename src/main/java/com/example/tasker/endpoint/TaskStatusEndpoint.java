package com.example.tasker.endpoint;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tasker.model.dto.TypeDTO;
import com.example.tasker.service.TaskStatusService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("api/task-statuses")
public class TaskStatusEndpoint {

    private final TaskStatusService taskStatusService;

    public TaskStatusEndpoint(TaskStatusService taskStatusService) {
        this.taskStatusService = taskStatusService;
    }

    @Operation(summary = "Retrieves all the task statuses")
    @GetMapping
    public List<TypeDTO> getAllTaskStatuses() {
        return taskStatusService.getAllTaskStatuses();
    }
}
