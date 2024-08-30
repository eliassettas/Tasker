package com.example.tasker.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.tasker.exception.CustomException;
import com.example.tasker.model.dto.TaskDTO;
import com.example.tasker.model.dto.TaskSearchCriteria;
import com.example.tasker.model.enums.TaskStatusName;
import com.example.tasker.model.persistance.QTask;
import com.example.tasker.model.persistance.SystemUser;
import com.example.tasker.model.persistance.Task;
import com.example.tasker.model.persistance.TaskStatus;
import com.example.tasker.model.persistance.UserProfile;
import com.example.tasker.repository.TaskRepository;
import com.example.tasker.util.SecurityUtils;
import com.example.tasker.util.UserUtils;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;

@Service
public class TaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final SystemUserService systemUserService;
    private final TaskStatusService taskStatusService;

    public TaskService(
            TaskRepository taskRepository,
            SystemUserService systemUserService,
            TaskStatusService taskStatusService
    ) {
        this.taskRepository = taskRepository;
        this.systemUserService = systemUserService;
        this.taskStatusService = taskStatusService;
    }

    public TaskDTO getTaskDTOById(Long id) throws CustomException {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Task not found"));
        return convertTaskToDTO(task);
    }

    public Task getTaskById(Long id) throws CustomException {
        Optional<Task> task = taskRepository.findById(id);
        return task.orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    public List<TaskDTO> getTasksBySearchCriteria(TaskSearchCriteria taskSearchCriteria) throws CustomException {
        List<Predicate> predicates = new ArrayList<>();
        if (taskSearchCriteria.getAssigneeId() != null) {
            Predicate predicate = QTask.task.assignee.id.eq(taskSearchCriteria.getAssigneeId());
            predicates.add(predicate);
        }
        if (taskSearchCriteria.getDescription() != null && !taskSearchCriteria.getDescription().isEmpty()) {
            Predicate predicate = QTask.task.description.like(taskSearchCriteria.getDescription());
            predicates.add(predicate);
        }
        if (taskSearchCriteria.getStatusName() != null && !taskSearchCriteria.getStatusName().isEmpty()) {
            Predicate predicate = QTask.task.taskStatus.name.eq(taskSearchCriteria.getStatusName());
            predicates.add(predicate);
        }

        Predicate predicate = ExpressionUtils.allOf(predicates);
        if (predicate == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Failed to create a predicate from the provided criteria");
        }

        List<TaskDTO> taskDTOs = new ArrayList<>();
        taskRepository.findAll(predicate).forEach(task -> taskDTOs.add(convertTaskToDTO(task)));
        return taskDTOs;
    }

    @Transactional(rollbackOn = CustomException.class)
    public TaskDTO createTask(TaskDTO taskDTO) throws CustomException {
        doBasicValidationChecks(taskDTO);

        SystemUser assignee = systemUserService.getUserById(taskDTO.getAssigneeId());
        TaskStatus taskStatus = taskStatusService.getTaskStatusByName(TaskStatusName.NEW.getValue());

        LOGGER.info("Creating task with name: {} for user id: {}", taskDTO.getName(), assignee.getId());
        Task newTask = new Task();
        populateTask(newTask, taskDTO, assignee, taskStatus);

        Task createdTask = taskRepository.save(newTask);
        return convertTaskToDTO(createdTask);
    }

    @Transactional(rollbackOn = CustomException.class)
    public TaskDTO updateTask(TaskDTO taskDTO) throws CustomException {
        Task task = getTaskById(taskDTO.getId());
        SecurityUtils.validateUser(task.getAssignee().getId());

        LocalDateTime lastUpdateDate = taskDTO.getLastUpdateDate();
        if (lastUpdateDate == null || task.getLastUpdateDate().isAfter(taskDTO.getLastUpdateDate())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Task data are out-dated");
        }

        doBasicValidationChecks(taskDTO);

        SystemUser assignee = systemUserService.getUserById(taskDTO.getAssigneeId());
        TaskStatus taskStatus = taskStatusService.getTaskStatusByName(taskDTO.getTaskStatus().getName());
        LOGGER.info("Updating task with id: {}", taskDTO.getId());
        populateTask(task, taskDTO, assignee, taskStatus);

        Task updatedTask = taskRepository.save(task);
        return convertTaskToDTO(updatedTask);
    }

    public void deleteTask(Long id) throws CustomException {
        Task task = getTaskById(id);
        SecurityUtils.validateUser(task.getAssignee().getId());
        LOGGER.info("Deleting task with id: {}", id);
        taskRepository.deleteById(task.getId());
    }

    private TaskDTO convertTaskToDTO(Task task) {
        TaskDTO taskDTO = TaskDTO.fromObject(task);
        SystemUser assignee = task.getAssignee();
        UserProfile userProfile = assignee.getProfile();
        String assigneeName = UserUtils.constructName(userProfile.getFirstName(), userProfile.getLastName());
        taskDTO.setAssigneeId(assignee.getId());
        taskDTO.setAssigneeName(assigneeName);
        return taskDTO;
    }

    private void doBasicValidationChecks(TaskDTO taskDTO) throws CustomException {
        String name = taskDTO.getName();
        if (!StringUtils.hasText(name)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Task name is empty");
        }

        String description = taskDTO.getDescription();
        if (!StringUtils.hasText(description)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Task description is empty");
        }

        if (taskDTO.getAssigneeId() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Task assignee is empty");
        }

        if (taskDTO.getId() != null && taskDTO.getTaskStatus() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Task status is empty");
        }
    }

    private void populateTask(Task task, TaskDTO taskDTO, SystemUser assignee, TaskStatus taskStatus) {
        task.setName(taskDTO.getName());
        task.setDescription(taskDTO.getDescription());
        task.setAssignee(assignee);
        if (taskStatus != null) {
            task.setTaskStatus(taskStatus);
        }
    }
}
