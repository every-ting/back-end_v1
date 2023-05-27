package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupMember;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.MemberRole;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    Optional<GroupMember> findByGroupAndMember(Group group, User user);

    Optional<GroupMember> findByGroupAndMemberAndRole(Group group, User user, MemberRole role);

    boolean existsByGroupAndMember(Group group, User member);

    boolean existsByGroupAndMemberAndRole(Group group, User member, MemberRole role);

    boolean existsByMemberAndRole(User member, MemberRole role);

    long countByGroup(Group group);

    @Query(value = "select entity from GroupMember entity join fetch entity.member where entity.group = :group")
    List<GroupMember> findAllByGroup(@Param("group") Group group);

    @Query(value = "select entity.group.id from GroupMember entity where entity.member = :user")
    List<Long> findAllGroupIdByMember(@Param("user") User user);

    @Query(value = "select entity from GroupMember entity join fetch entity.member " +
            "where entity.group = :group " +
            "and entity.role != com.ting.ting.domain.constant.MemberRole.LEADER " +
            "and entity.member not in (select m.member from GroupMember m where entity.role = com.ting.ting.domain.constant.MemberRole.LEADER)")
    List<GroupMember> findAvailableMemberAsALeaderInGroup(@Param("group") Group group, Pageable pageable);
}
