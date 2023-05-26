package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {

    Optional<GroupInvitation> findById(Long id);

    Optional<GroupInvitation> findByGroupMember_GroupAndInvitationCode(Group group, String invitationCode);

    List<GroupInvitation> findByGroupMember_Group(Group group);

    @Query(value = "select entity from GroupInvitation entity join fetch entity.groupMember WHERE entity.expiredAt < :now")
    List<GroupInvitation> findExpiredInvitationsForCleanup(@Param("now") LocalDateTime now);
}
