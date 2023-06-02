package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.custom.GroupWithMemberCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByGroupName(String name);

    @EntityGraph(attributePaths = {"groupMembers", "groupMembers.member"})
    Optional<Group> findById(Long groupId);

    Page<Group> findAllByGenderAndIsJoinableAndIsMatchedAndMemberSizeLimit(Gender gender, boolean isJoinable, boolean isMatched, int memberSizeLimit, Pageable pageable);

    @EntityGraph(attributePaths = {"groupMembers", "groupMembers.member"})
    List<Group> findAllWithMembersInfoByIdIn(List<Long> groupIds);

    @Query(value = "select new com.ting.ting.domain.custom.GroupWithMemberCount(entity.id, entity.groupName, entity.gender, count(gm), entity.memberSizeLimit, entity.school, entity.isMatched, entity.isJoinable, entity.memo, entity.createdAt) " +
            "from Group entity left join GroupMember gm on gm.group = entity where entity.id in :groupIds group by entity.id")
    List<GroupWithMemberCount> findAllWithMemberCountByIdIn(@Param("groupIds") List<Long> groupIds);

    @Query(value = "select new com.ting.ting.domain.custom.GroupWithMemberCount(entity.id, entity.groupName, entity.gender, count(gm), entity.memberSizeLimit, entity.school, entity.isMatched, entity.isJoinable, entity.memo, entity.createdAt) " +
            "from Group entity left join GroupMember gm on gm.group = entity group by entity.id")
    Page<GroupWithMemberCount> findAllWithMemberCount(Pageable pageable);


    @Query(value = "select new com.ting.ting.domain.custom.GroupWithMemberCount(entity.id, entity.groupName, entity.gender, count(gm), entity.memberSizeLimit, entity.school, entity.isMatched, entity.isJoinable, entity.memo, entity.createdAt) " +
            "from Group entity left join GroupMember gm on gm.group = entity " +
            "where entity.gender = :gender and entity.isJoinable = :isJoinable and entity not in (select gm2.group from GroupMember gm2 where gm2.member = :user) group by entity.id")
    Page<GroupWithMemberCount> findAllJoinableGroupWithMemberCountByGenderAndIsJoinableAndNotGroupMembers_Member(@Param("gender") Gender gender, @Param("isJoinable") boolean isJoinable, @Param("user") User user, Pageable pageable);
}
