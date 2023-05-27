package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupLikeToJoin;
import com.ting.ting.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupLikeToJoinRepository extends JpaRepository<GroupLikeToJoin, Long> {

    boolean existsByFromUserAndToGroup(User fromUser, Group toGroup);

}
