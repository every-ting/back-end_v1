package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query(value = "SELECT distinct entity FROM Group entity JOIN FETCH entity.leader",
        countQuery = "SELECT count(entity) FROM Group entity")
    Page<Group> findAll(Pageable pageable);

    Optional<Group> findByGroupName(String name);
}
