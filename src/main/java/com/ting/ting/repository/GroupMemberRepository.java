package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupMember;
import com.ting.ting.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    @Query(value = "SELECT entity.group from GroupMember entity where entity.member = :user and entity.status = com.ting.ting.domain.constant.MemberStatus.ACTIVE")
    List<Group> findAllGroupByMemberAndStatusActive(@Param("user") User user);

    List<GroupMember> findAllByGroup(Group group);
}
