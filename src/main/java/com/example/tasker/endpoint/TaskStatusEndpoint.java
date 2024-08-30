package com.example.tasker.endpoint;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tasker.model.dto.TypeDTO;
import com.example.tasker.service.TaskStatusService;

@RestController
@RequestMapping("api/task-statuses")
public class TaskStatusEndpoint {

    private final TaskStatusService taskStatusService;

    public TaskStatusEndpoint(TaskStatusService taskStatusService) {
        this.taskStatusService = taskStatusService;
    }

    @GetMapping
    public List<TypeDTO> getAllTaskStatuses() {
        return taskStatusService.getAllTaskStatuses();
    }
}
