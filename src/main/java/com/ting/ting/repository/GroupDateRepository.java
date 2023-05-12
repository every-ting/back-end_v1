package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupDateRepository extends JpaRepository<GroupDate, Long> {

    boolean existsByMenGroupOrWomenGroup(Group menGroup, Group womenGroup);
}
