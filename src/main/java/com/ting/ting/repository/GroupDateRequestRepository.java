package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupDateRequest;
import com.ting.ting.domain.custom.GroupIdWithLikeCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupDateRequestRepository extends JpaRepository<GroupDateRequest, Long> {

    boolean existsByFromGroupAndToGroup(Group fromGroup, Group toGroup);

    void deleteByFromGroupAndToGroup(Group fromGroup, Group toGroup);

    void deleteByFromGroup_IdAndToGroup_Id(Long fromGroupId, Long toGroupId);

    List<GroupDateRequest> findAllByFromGroup(Group fromGroup);

    Page<GroupDateRequest> findAllByToGroup(Group fromGroup, Pageable pageable);

    @Query(value = "select distinct entity.fromGroup from GroupDateRequest entity join entity.fromGroup where entity.toGroup = :toGroup")
    List<Group> findFromGroupByToGroup(@Param("toGroup") Group toGroup);

    @Query(value = "select distinct entity.toGroup from GroupDateRequest entity join entity.toGroup where entity.fromGroup = :fromGroup")
    List<Group> findToGroupByFromGroup(@Param("fromGroup") Group fromGroup);
}
