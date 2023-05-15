package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupDateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupDateRequestRepository extends JpaRepository<GroupDateRequest, Long> {

    boolean existsByFromGroupAndToGroup(Group fromGroup, Group toGroup);

    void deleteByFromGroupAndToGroup(Group fromGroup, Group toGroup);

    @Query(value = "select entity from GroupDateRequest entity join fetch entity.fromGroup where entity.toGroup = :toGroup")
    List<GroupDateRequest> findByToGroup(@Param("toGroup") Group toGroup);
    @Query(value = "select distinct entity.fromGroup from GroupDateRequest entity join entity.fromGroup where entity.toGroup = :toGroup")
    List<Group> findFromGroupByToGroup(@Param("toGroup") Group toGroup);

    @Query(value = "select distinct entity.toGroup from GroupDateRequest entity join entity.toGroup where entity.fromGroup = :fromGroup")
    List<Group> findToGroupByFromGroup(@Param("fromGroup") Group fromGroup);
}
