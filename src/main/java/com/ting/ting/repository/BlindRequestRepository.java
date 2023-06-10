package com.ting.ting.repository;

import com.ting.ting.domain.BlindRequest;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface BlindRequestRepository extends JpaRepository<BlindRequest, Long> {

    @Override
    Optional<BlindRequest> findById(Long id);

    Optional<BlindRequest> findByFromUserAndToUser(User fromUser, User toUser);

    Optional<BlindRequest> findByFromUser_IdAndToUser_Id(Long fromUserId, Long toUserId);

    Set<BlindRequest> findAllByFromUserAndStatus(User fromUser, RequestStatus status);

    Set<BlindRequest> findAllByToUserAndStatus(User toUser, RequestStatus status);

    Long countByFromUserAndStatus(User fromUser, RequestStatus status);
}
