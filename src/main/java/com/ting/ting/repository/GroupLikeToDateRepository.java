package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupLikeToDate;
import com.ting.ting.domain.GroupMember;
import com.ting.ting.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupLikeToDateRepository extends JpaRepository<GroupLikeToDate, Long> {

    void deleteByFromGroupMemberAndToGroup(GroupMember groupMember, Group toGroup);

    boolean existsByFromGroupMemberAndToGroup(GroupMember fromGroupMember, Group toGroup);

    @Query(value = "select entity.toGroup.id from GroupLikeToDate entity where entity.fromGroupMember.group = :group and entity.fromGroupMember.member = :member")
    List<Long> findAllToGroup_IdByFromGroupMember_GroupAndGroupMember_Member(@Param("group") Group group, @Param("member") User member);
}
