package com.ting.ting.service;

import com.ting.ting.domain.BlindRequest;
import com.ting.ting.domain.User;
import com.ting.ting.exception.DBException;
import com.ting.ting.repository.BlindRequestRepository;
import com.ting.ting.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class BlindRequestService {

    private final UserRepository userRepository;
    private final BlindRequestRepository blindRequestRepository;

    public BlindRequestService(UserRepository userRepository, BlindRequestRepository blindRequestRepository) {
        this.userRepository = userRepository;
        this.blindRequestRepository = blindRequestRepository;
    }

    public void createJoinRequest(long fromUserId, long toUserId) {
        User fromUser = userRepository.findById(fromUserId).orElseThrow(() -> new DBException("잘못된 정보 입력"));
        User toUser = userRepository.findById(toUserId).orElseThrow(() -> new DBException("잘못된 정보 입력"));

        if (fromUser.equals(toUser)) {
            throw new DBException("잘못된 정보 입력");
        }

        BlindRequest request = new BlindRequest();
        request.setFromUser(fromUser);
        request.setToUser(toUser);
        blindRequestRepository.save(request);
    }
}
