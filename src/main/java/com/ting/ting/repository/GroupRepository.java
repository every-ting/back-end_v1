package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.custom.GroupWithMemberCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByGroupName(String name);

    @Query(value = "select new com.ting.ting.domain.custom.GroupWithMemberCount(entity.id, entity.groupName, entity.gender, count(gm), entity.memberSizeLimit, entity.school, entity.isMatched, entity.isJoinable, entity.memo) " +
            "from Group entity left join GroupMember gm on gm.group = entity " +
            "group by entity.id, entity.groupName, entity.gender, entity.gender, entity.school, entity.memberSizeLimit, entity.isMatched, entity.isJoinable, entity.memo")
    Page<GroupWithMemberCount> findAllWithMemberCount(Pageable pageable);

    @Query(value = "select new com.ting.ting.domain.custom.GroupWithMemberCount(entity.id, entity.groupName, entity.gender, count(gm), entity.memberSizeLimit, entity.school, entity.isMatched, entity.isJoinable, entity.memo) " +
            "from Group entity left join GroupMember gm on gm.group = entity " +
            "where entity.gender = :gender and entity.isMatched = :isMatched " +
            "group by entity.id, entity.groupName, entity.gender, entity.gender, entity.school, entity.memberSizeLimit, entity.isMatched, entity.isJoinable, entity.memo")
    Page<GroupWithMemberCount> findAllWithMemberCountByGenderAndIsMatched(@Param("gender") Gender gender, @Param("isMatched") boolean isMatched, Pageable pageable);

    @Query(value = "select new com.ting.ting.domain.custom.GroupWithMemberCount(entity.id, entity.groupName, entity.gender, count(gm), entity.memberSizeLimit, entity.school, entity.isMatched, entity.isJoinable, entity.memo) " +
            "from Group entity left join GroupMember gm on gm.group = entity " +
            "where entity.gender = :gender and entity.isJoinable = :isJoinable and entity not in (select gm2.group from GroupMember gm2 where gm2.member = :user) " +
            "group by entity.id, entity.groupName, entity.gender, entity.gender, entity.school, entity.memberSizeLimit, entity.isMatched, entity.isJoinable, entity.memo")
    Page<GroupWithMemberCount> findAllJoinableGroupWithMemberCountByGenderAndIsJoinableAndNotGroupMembers_Member(@Param("gender") Gender gender, @Param("isJoinable") boolean isJoinable, @Param("user") User user, Pageable pageable);
}
