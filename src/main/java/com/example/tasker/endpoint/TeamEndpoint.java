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
import com.example.tasker.model.dto.TeamDTO;
import com.example.tasker.model.dto.TeamMemberRelationshipDTO;
import com.example.tasker.service.TeamService;

@RestController
@RequestMapping("api/teams")
public class TeamEndpoint {

    private final TeamService teamService;

    public TeamEndpoint(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/{id}")
    public TeamDTO getTeamById(@PathVariable("id") Long id) throws CustomException {
        return teamService.getTeamDTOById(id);
    }

    @GetMapping("/users/{userId}")
    public List<TeamDTO> getTeamsByUser(@PathVariable("userId") Integer userId) {
        return teamService.getTeamsByUser(userId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_TEAM')")
    public ResponseEntity<TeamDTO> createTeam(@RequestBody TeamDTO teamDTO) throws CustomException {
        TeamDTO createdTeam = teamService.createTeam(teamDTO);
        return new ResponseEntity<>(createdTeam, HttpStatus.CREATED);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('UPDATE_TEAM')")
    public ResponseEntity<TeamDTO> updateTeam(@RequestBody TeamDTO teamDTO) throws CustomException {
        TeamDTO updatedTeam = teamService.updateTeam(teamDTO);
        return new ResponseEntity<>(updatedTeam, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_TEAM')")
    public ResponseEntity<?> deleteTeam(@PathVariable("id") Long id) throws CustomException {
        teamService.deleteTeam(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/relationships")
    @PreAuthorize("hasAuthority('CREATE_TEAM_RELATIONSHIP')")
    public ResponseEntity<TeamMemberRelationshipDTO> createRelationship(@RequestBody TeamMemberRelationshipDTO teamMemberRelationshipDTO) throws CustomException {
        TeamMemberRelationshipDTO createdRelationship = teamService.createRelationship(teamMemberRelationshipDTO);
        return new ResponseEntity<>(createdRelationship, HttpStatus.CREATED);
    }

    @DeleteMapping("/relationships")
    @PreAuthorize("hasAuthority('DELETE_TEAM_RELATIONSHIP')")
    public ResponseEntity<?> deleteRelationship(@RequestBody TeamMemberRelationshipDTO teamMemberRelationshipDTO) throws CustomException {
        teamService.deleteRelationship(teamMemberRelationshipDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
