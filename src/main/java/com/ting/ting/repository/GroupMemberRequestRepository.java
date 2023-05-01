package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupMemberRequest;
import com.ting.ting.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupMemberRequestRepository extends JpaRepository<GroupMemberRequest, Long> {

    Optional<GroupMemberRequest> findByGroupAndUser(Group group, User user);

    void deleteByGroup_IdAndUser_Id(Long groupId, Long userId);
}
