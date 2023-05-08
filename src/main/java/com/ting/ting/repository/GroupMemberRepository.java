package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupMember;
import com.ting.ting.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    @Query(value = "select entity.group from GroupMember entity where entity.member = :user and entity.status = com.ting.ting.domain.constant.MemberStatus.ACTIVE")
    List<Group> findAllGroupByMemberAndStatusActive(@Param("user") User user);

    List<GroupMember> findAllByGroup(Group group);

    @Query(value = "select entity from GroupMember entity where entity.group = :group and entity.status = com.ting.ting.domain.constant.MemberStatus.ACTIVE")
    List<GroupMember> findAllByGroupAndStatusActive(@Param("group") Group group);

    @Query(value = "select entity from GroupMember entity where entity.group = :group and entity.member = :user and entity.status = com.ting.ting.domain.constant.MemberStatus.ACTIVE")
    Optional<GroupMember> findByGroupAndMemberAndStatusActive(@Param("group") Group group, @Param("user") User user);
}
