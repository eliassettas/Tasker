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
import com.example.tasker.model.dto.CommentDTO;
import com.example.tasker.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("api/comments")
public class CommentEndpoint {

    private final CommentService commentService;

    public CommentEndpoint(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Retrieves the comments associated with a task")
    @GetMapping("/tasks/{task_id}")
    public List<CommentDTO> getCommentsByTask(@PathVariable("task_id") Long taskId) {
        return commentService.getCommentsByTask(taskId);
    }

    @Operation(summary = "Creates a comment")
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_COMMENT')")
    public ResponseEntity<CommentDTO> createComment(@RequestBody CommentDTO commentDTO) throws CustomException {
        CommentDTO createdComment = commentService.createComment(commentDTO);
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    @Operation(summary = "Updates a comment")
    @PutMapping
    @PreAuthorize("hasAuthority('UPDATE_COMMENT')")
    public ResponseEntity<CommentDTO> updateComment(@RequestBody CommentDTO commentDTO) throws CustomException {
        CommentDTO updatedComment = commentService.updateComment(commentDTO);
        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    @Operation(summary = "Deletes a comment")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_COMMENT')")
    public ResponseEntity<?> deleteComment(@PathVariable("id") Long id) throws CustomException {
        commentService.deleteComment(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
