package com.ting.ting.repository;

import com.ting.ting.domain.BlindLike;
import com.ting.ting.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface BlindLikeRepository extends JpaRepository<BlindLike, Long> {

    Optional<BlindLike> findById(Long blindLikeId);

    Optional<BlindLike> findByFromUserAndToUser(User fromUser, User toUser);

    Optional<BlindLike> findByFromUser_IdAndToUser_Id(long userId, long toUserId);

    Set<BlindLike> findAllByFromUser(User user);
}
