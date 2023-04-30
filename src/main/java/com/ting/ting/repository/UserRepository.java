package com.ting.ting.repository;

import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findAllByGender(Gender gender, Pageable pageable);

    @Override
    Optional<User> findById(Long id);
}
