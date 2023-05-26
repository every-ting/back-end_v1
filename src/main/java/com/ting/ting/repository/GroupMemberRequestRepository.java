package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupMemberRequest;
import com.ting.ting.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRequestRepository extends JpaRepository<GroupMemberRequest, Long> {

    @Query(value = "select entity.group.id from GroupMemberRequest entity where entity.user = :user")
    List<Long> findAllGroup_IdByUser(@Param("user") User user);

    Optional<GroupMemberRequest> findByGroupAndUser(Group group, User user);

    void deleteByGroup_IdAndUser_Id(Long groupId, Long userId);

    @Query(value = "select entity from GroupMemberRequest entity join fetch entity.user where entity.group = :group")
    List<GroupMemberRequest> findByGroup(@Param("group") Group group);
}
