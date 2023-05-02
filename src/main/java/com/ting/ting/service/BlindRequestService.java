package com.ting.ting.service;

import com.ting.ting.domain.BlindRequest;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.exception.UserException;
import com.ting.ting.repository.BlindRequestRepository;
import com.ting.ting.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class BlindRequestService {

    private final UserRepository userRepository;
    private final BlindRequestRepository blindRequestRepository;

    public BlindRequestService(UserRepository userRepository, BlindRequestRepository blindRequestRepository) {
        this.userRepository = userRepository;
        this.blindRequestRepository = blindRequestRepository;
    }

    public void createJoinRequest(long fromUserId, long toUserId) {
        if (Objects.equals(fromUserId, toUserId)) {
            throw new UserException("같은 사용자간의 요청 처리입니다.");
        }

        User fromUser = userRepository.findById(fromUserId).orElseThrow(() -> new UserException("해당 사용자의 정보가 존재하지 않습니다."));
        User toUser = userRepository.findById(toUserId).orElseThrow(() -> new UserException("해당 사용자의 정보가 존재하지 않습니다."));

        blindRequestRepository.findByFromUserAndToUser(fromUser, toUser).ifPresent(it -> {
            throw new UserException("이전에 보낸 요청이 있습니다.");
        });

        BlindRequest request = new BlindRequest();
        request.setFromUser(fromUser);
        request.setToUser(toUser);
        request.setStatus(RequestStatus.PENDING);
        blindRequestRepository.save(request);
    }

    public void deleteRequest(long blindRequestId) {
        BlindRequest request = blindRequestRepository.findById(blindRequestId).orElseThrow(() -> new UserException("해당 요청 정보가 존재하지 않습니다."));
        blindRequestRepository.delete(request);
    }

    public void acceptRequest(long blindRequestId) {
        BlindRequest request = blindRequestRepository.findById(blindRequestId).orElseThrow(() -> new UserException("해당 요청 정보가 존재하지 않습니다."));
        request.setStatus(RequestStatus.ACCEPTED);
        blindRequestRepository.save(request);
    }

    public void rejectRequest(long blindRequestId) {
        BlindRequest request = blindRequestRepository.findById(blindRequestId).orElseThrow(() -> new UserException("해당 요청 정보가 존재하지 않습니다."));
        request.setStatus(RequestStatus.REJECTED);
        blindRequestRepository.save(request);
    }
}
