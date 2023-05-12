package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupMember;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    Optional<GroupMember> findByGroupAndRole(Group group, MemberRole role);

    Optional<GroupMember> findByGroupAndMemberAndStatus(Group group, User user, MemberStatus status);

    Optional<GroupMember> findByGroupAndMemberAndStatusAndRole(Group group, User user, MemberStatus status, MemberRole role);

    boolean existsByGroupAndMember(Group group, User member);

    long countByGroup(Group group);

    @Query(value = "select entity from GroupMember entity join fetch entity.member where entity.group = :group")
    List<GroupMember> findAllByGroup(@Param("group") Group group);

    @Query(value = "select entity.group from GroupMember entity where entity.member = :user and entity.status = :status")
    List<Group> findAllGroupByMemberAndStatus(@Param("user") User user, @Param("status") MemberStatus status);

    @Query(value = "select entity.group from GroupMember entity where entity.member = :member and entity.role = :role")
    Optional<Group> findGroupByMemberAndRole(@Param("member") User member, @Param("role") MemberRole role);
}
