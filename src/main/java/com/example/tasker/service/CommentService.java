package com.example.tasker.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.tasker.exception.CustomException;
import com.example.tasker.model.dto.CommentDTO;
import com.example.tasker.model.persistence.Comment;
import com.example.tasker.model.persistence.SystemUser;
import com.example.tasker.model.persistence.Task;
import com.example.tasker.model.persistence.UserProfile;
import com.example.tasker.repository.CommentRepository;
import com.example.tasker.util.SecurityUtils;
import com.example.tasker.util.UserUtils;

@Service
public class CommentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final SystemUserService systemUserService;
    private final TaskService taskService;

    public CommentService(
            CommentRepository commentRepository,
            TaskService taskService,
            SystemUserService systemUserService
    ) {
        this.commentRepository = commentRepository;
        this.systemUserService = systemUserService;
        this.taskService = taskService;
    }

    public List<CommentDTO> getCommentsByTask(Long taskId) {
        List<Comment> comments = commentRepository.findByTaskIdOrderByCreationDate(taskId);
        return comments.stream()
                .map(this::convertCommentToDTO)
                .collect(Collectors.toList());
    }

    public Comment getCommentById(Long id) throws CustomException {
        Optional<Comment> comment = commentRepository.findById(id);
        return comment.orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Comment not found"));
    }

    @Transactional(rollbackFor = CustomException.class)
    public CommentDTO createComment(CommentDTO commentDTO) throws CustomException {
        doBasicValidationChecks(commentDTO);
        SystemUser writer = systemUserService.getUserById(commentDTO.getWriterId());
        SecurityUtils.validateUser(writer.getId());

        Task task = taskService.getTaskById(commentDTO.getTaskId());

        LOGGER.info("Creating comment from user id: {} on task id: {}", commentDTO.getWriterId(), commentDTO.getTaskId());
        Comment newComment = new Comment();
        newComment.setDescription(commentDTO.getDescription());
        newComment.setWriter(writer);
        newComment.setTask(task);

        Comment createdComment = commentRepository.save(newComment);
        return convertCommentToDTO(createdComment);
    }

    @Transactional(rollbackFor = CustomException.class)
    public CommentDTO updateComment(CommentDTO commentDTO) throws CustomException {
        doBasicValidationChecks(commentDTO);
        Comment comment = getCommentById(commentDTO.getId());
        SecurityUtils.validateUser(comment.getWriter().getId());

        LocalDateTime lastUpdateDate = commentDTO.getLastUpdateDate();
        if (lastUpdateDate == null || comment.getLastUpdateDate().isAfter(commentDTO.getLastUpdateDate())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Comment data are out-dated");
        }

        LOGGER.info("Updating comment with id: {}", commentDTO.getId());
        comment.setDescription(commentDTO.getDescription());

        Comment updatedComment = commentRepository.saveAndFlush(comment);
        return convertCommentToDTO(updatedComment);
    }

    public void deleteComment(Long id) throws CustomException {
        Comment comment = getCommentById(id);
        SecurityUtils.validateUser(comment.getWriter().getId());
        commentRepository.deleteById(comment.getId());
    }

    private CommentDTO convertCommentToDTO(Comment comment) {
        CommentDTO commentDTO = CommentDTO.fromObject(comment);
        commentDTO.setTaskId(comment.getTask().getId());
        SystemUser writer = comment.getWriter();
        commentDTO.setWriterId(writer.getId());
        UserProfile userProfile = writer.getProfile();
        String writerName = UserUtils.constructName(userProfile.getFirstName(), userProfile.getLastName());
        commentDTO.setWriterName(writerName);
        return commentDTO;
    }

    private void doBasicValidationChecks(CommentDTO commentDTO) throws CustomException {
        String description = commentDTO.getDescription();
        if (!StringUtils.hasText(description)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Comment description is empty");
        }

        if (commentDTO.getWriterId() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Comment writer is empty");
        }

        if (commentDTO.getId() == null && commentDTO.getTaskId() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Comment is not associated with a task");
        }
    }
}
