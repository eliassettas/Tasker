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

import com.example.tasker.exception.CustomException;
import com.example.tasker.model.dto.TeamDTO;
import com.example.tasker.model.dto.TeamMemberRelationshipDTO;
import com.example.tasker.model.enums.TeamRoleName;
import com.example.tasker.model.persistence.SystemUser;
import com.example.tasker.model.persistence.Team;
import com.example.tasker.model.persistence.TeamMemberRelationship;
import com.example.tasker.repository.TeamMemberRelationshipRepository;
import com.example.tasker.repository.TeamRepository;
import com.example.tasker.util.SecurityUtils;

@Service
public class TeamService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamService.class);

    private final TeamRepository teamRepository;
    private final TeamMemberRelationshipRepository teamMemberRelationshipRepository;
    private final SystemUserService systemUserService;

    public TeamService(
            TeamRepository teamRepository,
            TeamMemberRelationshipRepository teamMemberRelationshipRepository,
            SystemUserService systemUserService
    ) {
        this.teamRepository = teamRepository;
        this.teamMemberRelationshipRepository = teamMemberRelationshipRepository;
        this.systemUserService = systemUserService;
    }

    public TeamDTO getTeamDTOById(Long teamId) throws CustomException {
        Team team = getTeamById(teamId);
        return convertTeamToDTO(team);
    }

    public Team getTeamById(Long teamId) throws CustomException {
        Optional<Team> team = teamRepository.findById(teamId);
        return team.orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Team not found"));
    }

    public List<TeamDTO> getTeamsByUser(Integer userId) {
        List<Team> teams = teamRepository.findByUser(userId);
        return teams.stream().map(TeamDTO::fromObject).collect(Collectors.toList());
    }

    private TeamDTO convertTeamToDTO(Team team) {
        TeamDTO teamDTO = TeamDTO.fromObject(team);
        List<TeamMemberRelationship> relationships = teamMemberRelationshipRepository.findByTeamId(team.getId());
        List<TeamMemberRelationshipDTO> relationshipDTOs = relationships.stream()
                .map(TeamMemberRelationshipDTO::fromObject)
                .collect(Collectors.toList());
        teamDTO.setMembers(relationshipDTOs);
        return teamDTO;
    }

    @Transactional(rollbackFor = CustomException.class)
    public TeamDTO createTeam(TeamDTO teamDTO) throws CustomException {
        if (teamDTO.getName() == null || teamDTO.getName().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Team name cannot be empty");
        }

        if (teamRepository.findByName(teamDTO.getName()).isPresent()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Team name is not available");
        }

        String username = SecurityUtils.getUsername();
        SystemUser systemUser = systemUserService.getUserByUsername(username);

        LOGGER.info("Creating team with name: {}", teamDTO.getName());
        Team newTeam = new Team();
        newTeam.setName(teamDTO.getName());
        Team createdTeam = teamRepository.save(newTeam);

        TeamMemberRelationship teamMemberRelationship = new TeamMemberRelationship();
        teamMemberRelationship.setTeam(createdTeam);
        teamMemberRelationship.setMember(systemUser);
        teamMemberRelationship.setRole(TeamRoleName.LEADER.getValue());
        teamMemberRelationship.setLeader(true);

        teamMemberRelationshipRepository.save(teamMemberRelationship);

        return convertTeamToDTO(createdTeam);
    }

    @Transactional(rollbackFor = CustomException.class)
    public TeamDTO updateTeam(TeamDTO teamDTO) throws CustomException {
        Team team = getTeamById(teamDTO.getId());
        checkUserRole(team.getId());

        LocalDateTime lastUpdateDate = teamDTO.getLastUpdateDate();
        if (lastUpdateDate == null || team.getLastUpdateDate().isAfter(teamDTO.getLastUpdateDate())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Team data are out-dated");
        }
        if (teamDTO.getName() == null || teamDTO.getName().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Team name cannot be empty");
        }

        LOGGER.info("Updating team with id: {}", team.getId());
        team.setName(teamDTO.getName());
        Team updatedTeam = teamRepository.saveAndFlush(team);

        return convertTeamToDTO(updatedTeam);
    }

    public void deleteTeam(Long id) throws CustomException {
        Team team = getTeamById(id);
        checkUserRole(team.getId());
        LOGGER.info("Deleting team with id: {}", team.getId());
        teamRepository.deleteById(team.getId());
    }

    @Transactional(rollbackFor = CustomException.class)
    public TeamMemberRelationshipDTO createRelationship(TeamMemberRelationshipDTO teamMemberRelationshipDTO) throws CustomException {
        validateRelationship(teamMemberRelationshipDTO);
        String role = teamMemberRelationshipDTO.getRole();
        if (role == null || role.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Role must be provided");
        }

        Long teamId = teamMemberRelationshipDTO.getTeamId();
        Team team = getTeamById(teamId);
        checkUserRole(team.getId());

        Integer memberId = teamMemberRelationshipDTO.getMemberId();
        Optional<TeamMemberRelationship> teamMemberRelationship = teamMemberRelationshipRepository.findByTeamIdAndMemberId(teamId, memberId);
        if (teamMemberRelationship.isPresent()) {
            throw new CustomException(HttpStatus.CONFLICT, "Relationship already exists");
        }

        SystemUser member = systemUserService.getUserById(memberId);

        LOGGER.info("Creating team member relationship for team id: {} and user id: {}", team.getId(), member.getId());
        TeamMemberRelationship newTeamMemberRelationship = new TeamMemberRelationship();
        newTeamMemberRelationship.setTeam(team);
        newTeamMemberRelationship.setMember(member);
        newTeamMemberRelationship.setRole(role);
        newTeamMemberRelationship.setLeader(teamMemberRelationshipDTO.isLeader());

        TeamMemberRelationship savedTeamMemberRelationship = teamMemberRelationshipRepository.save(newTeamMemberRelationship);
        return TeamMemberRelationshipDTO.fromObject(savedTeamMemberRelationship);
    }

    @Transactional(rollbackFor = CustomException.class)
    public void deleteRelationship(TeamMemberRelationshipDTO teamMemberRelationshipDTO) throws CustomException {
        validateRelationship(teamMemberRelationshipDTO);

        Integer memberId = teamMemberRelationshipDTO.getMemberId();
        Long teamId = teamMemberRelationshipDTO.getTeamId();
        SystemUser systemUser = systemUserService.getUserByUsername(SecurityUtils.getUsername());
        if (!systemUser.getId().equals(memberId)) {
            checkUserRole(systemUser.getId(), teamId);
        }

        TeamMemberRelationship teamMemberRelationship = teamMemberRelationshipRepository.findByTeamIdAndMemberId(teamId, memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Relationship not found"));

        Team team = teamMemberRelationship.getTeam();
        boolean isSelfDelete = systemUser.getId().equals(memberId);
        boolean isLastLeader = isSelfDelete && team.getRelationships().stream()
                .filter(TeamMemberRelationship::isLeader)
                .count() < 2;
        if (isLastLeader) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Cannot leave a team as the last remaining leader");
        }

        boolean deleteTeam = team.getRelationships().size() < 2;

        LOGGER.info("Deleting team member relationship with id: {}", teamMemberRelationship.getId());
        teamMemberRelationshipRepository.delete(teamMemberRelationship);

        if (deleteTeam) {
            LOGGER.info("Deleting team with id {} due to zero remaining members", team.getId());
            teamRepository.deleteById(team.getId());
        }
    }

    private void validateRelationship(TeamMemberRelationshipDTO teamMemberRelationshipDTO) throws CustomException {
        if (teamMemberRelationshipDTO.getMemberId() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "User id must be provided");
        }
        if (teamMemberRelationshipDTO.getTeamId() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Team id must be provided");
        }
    }

    private void checkUserRole(Long teamId) throws CustomException {
        SystemUser systemUser = systemUserService.getUserByUsername(SecurityUtils.getUsername());
        checkUserRole(systemUser.getId(), teamId);
    }

    private void checkUserRole(Integer memberId, Long teamId) throws CustomException {
        TeamMemberRelationship teamMemberRelationship = teamMemberRelationshipRepository.findByTeamIdAndMemberId(teamId, memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "Action cannot be performed by a user who is not part of the team"));

        if (!teamMemberRelationship.isLeader()) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "Action cannot be performed by a user who is not a team leader");
        }
    }
}
