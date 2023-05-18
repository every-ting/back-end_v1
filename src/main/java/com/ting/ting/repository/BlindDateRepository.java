package com.ting.ting.repository;

import com.ting.ting.domain.BlindDate;
import com.ting.ting.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlindDateRepository extends JpaRepository<BlindDate, Long> {

    @Query(value = "SELECT count(bd) FROM BlindDate bd WHERE bd.menUser = :fromUser OR bd.womenUser = :fromUser")
    Long countByBlindDate(@Param("fromUser") User user);
}
