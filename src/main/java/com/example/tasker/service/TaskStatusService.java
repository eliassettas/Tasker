package com.example.tasker.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.tasker.exception.CustomException;
import com.example.tasker.model.dto.TypeDTO;
import com.example.tasker.model.persistence.TaskStatus;
import com.example.tasker.repository.TaskStatusRepository;

@Service
public class TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;

    public TaskStatusService(TaskStatusRepository taskStatusRepository) {
        this.taskStatusRepository = taskStatusRepository;
    }

    public List<TypeDTO> getAllTaskStatuses() {
        List<TaskStatus> taskStatuses = taskStatusRepository.findAll();
        return taskStatuses.stream()
                .map(TypeDTO::fromObject)
                .collect(Collectors.toList());
    }

    public TaskStatus getTaskStatusByName(String name) throws CustomException {
        Optional<TaskStatus> taskStatus = taskStatusRepository.findByName(name);
        return taskStatus.orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Task Status not found"));
    }
}
