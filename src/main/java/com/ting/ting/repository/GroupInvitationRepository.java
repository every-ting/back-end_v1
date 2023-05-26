package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {

    List<GroupInvitation> findByGroupMember_Group(Group group);

    Optional<GroupInvitation> findByGroupMember_GroupAndInvitationCode(Group group, String invitationCode);
}
