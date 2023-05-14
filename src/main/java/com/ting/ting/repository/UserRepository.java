package com.ting.ting.repository;

import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findAllByGenderAndIdNotIn(Gender gender, Set<Long> usersId, Pageable pageable);

    @Override
    Optional<User> findById(Long id);
}
