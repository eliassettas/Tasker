package com.example.tasker.endpoint;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tasker.exception.CustomException;
import com.example.tasker.model.dto.TeamMemberRelationshipDTO;
import com.example.tasker.service.TeamMemberRelationshipService;

@RestController
@RequestMapping("api/team-member-relationships")
public class TeamMemberRelationshipEndpoint {

    private final TeamMemberRelationshipService teamMemberRelationshipService;

    public TeamMemberRelationshipEndpoint(TeamMemberRelationshipService teamMemberRelationshipService) {
        this.teamMemberRelationshipService = teamMemberRelationshipService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_TEAM_RELATIONSHIP')")
    public ResponseEntity<String> createRelationship(@RequestBody TeamMemberRelationshipDTO teamMemberRelationshipDTO) throws CustomException {
        teamMemberRelationshipService.createRelationship(teamMemberRelationshipDTO);
        return new ResponseEntity<>("Team - Member relationship has been created", HttpStatus.CREATED);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('DELETE_TEAM_RELATIONSHIP')")
    public ResponseEntity<Object> deleteRelationship(@RequestBody TeamMemberRelationshipDTO teamMemberRelationshipDTO) throws CustomException {
        teamMemberRelationshipService.deleteRelationship(teamMemberRelationshipDTO);
        return new ResponseEntity<>("Team - Member relationship has been deleted", HttpStatus.OK);
    }
}
