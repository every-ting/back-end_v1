package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupLikeToJoin;
import com.ting.ting.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupLikeToJoinRepository extends JpaRepository<GroupLikeToJoin, Long> {

    boolean existsByFromUserAndToGroup(User fromUser, Group toGroup);

    void deleteByFromUser_IdAndToGroup_Id(Long userId, Long groupId);

    @Query(value = "select entity.toGroup from GroupLikeToJoin entity where entity.fromUser = :fromUser")
    List<Group> findAllToGroupByFromUser(@Param("fromUser") User fromUser);
}
