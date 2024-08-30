package com.example.tasker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.tasker.model.persistence.TeamMemberRelationship;

@Repository
public interface TeamMemberRelationshipRepository extends JpaRepository<TeamMemberRelationship, Long> {

    List<TeamMemberRelationship> findByTeamId(Long teamId);

    Optional<TeamMemberRelationship> findByTeamIdAndMemberId(Long teamId, Integer memberId);
}
