package com.example.tasker.model.dto;

import com.example.tasker.model.persistence.TeamMemberRelationship;
import com.example.tasker.model.persistence.UserProfile;
import com.example.tasker.util.UserUtils;

public class TeamMemberRelationshipDTO {

    private Long teamId;
    private Integer memberId;
    private String memberName;
    private String role;
    private boolean isLeader;

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean isLeader) {
        this.isLeader = isLeader;
    }

    public static TeamMemberRelationshipDTO fromObject(TeamMemberRelationship relationship) {
        TeamMemberRelationshipDTO relationshipDTO = new TeamMemberRelationshipDTO();
        relationshipDTO.setTeamId(relationship.getTeam().getId());
        relationshipDTO.setMemberId(relationship.getMember().getId());
        UserProfile userProfile = relationship.getMember().getProfile();
        String memberName = UserUtils.constructName(userProfile.getFirstName(), userProfile.getLastName());
        relationshipDTO.setMemberName(memberName);
        relationshipDTO.setRole(relationship.getRole());
        relationshipDTO.setLeader(relationship.isLeader());
        return relationshipDTO;
    }
}
