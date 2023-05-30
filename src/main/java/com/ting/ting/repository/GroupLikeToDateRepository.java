package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupLikeToDate;
import com.ting.ting.domain.GroupMember;
import com.ting.ting.domain.User;
import com.ting.ting.domain.custom.GroupIdWithLikeCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupLikeToDateRepository extends JpaRepository<GroupLikeToDate, Long> {

    void deleteByFromGroupMemberAndToGroup(GroupMember groupMember, Group toGroup);

    boolean existsByFromGroupMemberAndToGroup(GroupMember fromGroupMember, Group toGroup);

    List<GroupLikeToDate> findAllByFromGroupMember(GroupMember fromGroupMember);

    @Query(value = "select new com.ting.ting.domain.custom.GroupIdWithLikeCount(entity.toGroup.id, count(*)) from GroupLikeToDate entity where entity.fromGroupMember.group = :fromGroup group by entity.toGroup.id")
    Page<GroupIdWithLikeCount> findAllToGroupIdAndLikeCountByFromGroupMember_Group(@Param("fromGroup") Group fromGroup, Pageable pageable);
}
