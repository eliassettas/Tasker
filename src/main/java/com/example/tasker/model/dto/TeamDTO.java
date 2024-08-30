package com.example.tasker.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.tasker.model.persistence.Team;

public class TeamDTO {

    private Long id;
    private String name;
    private List<TeamMemberRelationshipDTO> members;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TeamMemberRelationshipDTO> getMembers() {
        return members;
    }

    public void setMembers(List<TeamMemberRelationshipDTO> members) {
        this.members = members;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public static TeamDTO fromObject(Team team) {
        TeamDTO teamDTO = new TeamDTO();
        teamDTO.setId(team.getId());
        teamDTO.setName(team.getName());
        teamDTO.setCreationDate(team.getCreationDate());
        teamDTO.setLastUpdateDate(team.getLastUpdateDate());
        return teamDTO;
    }
}
