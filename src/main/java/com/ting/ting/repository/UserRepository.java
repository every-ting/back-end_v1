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

    @Override
    Optional<User> findById(Long id);

    Page<User> findAllByGenderAndIdNotIn(Gender gender, Set<Long> usersId, Pageable pageable);

    Optional<User> findBySocialEmail(String socialEmail);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
