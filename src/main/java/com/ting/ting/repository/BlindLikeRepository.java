package com.ting.ting.repository;

import com.ting.ting.domain.BlindLike;
import com.ting.ting.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlindLikeRepository extends JpaRepository<BlindLike, Long> {

    Optional<BlindLike> findByFromUserAndToUser(User fromUser, User toUser);
}
