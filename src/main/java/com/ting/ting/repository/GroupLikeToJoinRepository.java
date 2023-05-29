package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupLikeToJoin;
import com.ting.ting.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupLikeToJoinRepository extends JpaRepository<GroupLikeToJoin, Long> {

    boolean existsByFromUserAndToGroup(User fromUser, Group toGroup);

    void deleteByFromUser_IdAndToGroup_Id(Long userId, Long groupId);

    Page<GroupLikeToJoin> findAllByFromUser(User fromUser, Pageable pageable);

    List<GroupLikeToJoin> findAllByFromUser(User fromUser);
}
