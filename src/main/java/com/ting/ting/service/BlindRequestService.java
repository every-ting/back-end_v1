package com.ting.ting.service;

import com.ting.ting.domain.BlindRequest;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.exception.TingApplicationException;
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

    /**
     * 소개팅 상대에게 요청
     */
    public void createJoinRequest(long fromUserId, long toUserId) {
        if (Objects.equals(fromUserId, toUserId)) {
            throw new TingApplicationException(ErrorCode.DUPLICATED_USER_REQUEST, ServiceType.BLIND);
        }

        User fromUser = userRepository.findById(fromUserId).orElseThrow(()
                -> new TingApplicationException(ErrorCode.USER_NOT_FOUND, ServiceType.BLIND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", fromUserId)));
        User toUser = userRepository.findById(toUserId).orElseThrow(() ->
                new TingApplicationException(ErrorCode.USER_NOT_FOUND, ServiceType.BLIND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", toUserId)));

        blindRequestRepository.findByFromUserAndToUser(fromUser, toUser).ifPresent(it -> {
            throw new TingApplicationException(ErrorCode.DUPLICATED_REQUEST, ServiceType.BLIND);
        });

        BlindRequest request = new BlindRequest();
        request.setFromUser(fromUser);
        request.setToUser(toUser);
        request.setStatus(RequestStatus.PENDING);
        blindRequestRepository.save(request);
    }

    /**
     * 소개팅 상대에게 한 요청 취소
     */
    public void deleteRequest(long blindRequestId) {
        BlindRequest request = blindRequestRepository.findById(blindRequestId).orElseThrow(() ->
                new TingApplicationException(ErrorCode.REQUEST_NOT_FOUND, ServiceType.BLIND));

        blindRequestRepository.delete(request);
    }

    /**
     * 자신에게 온 요청 수락 -> 추가 구현 필요
     */
    public void acceptRequest(long blindRequestId) {
        BlindRequest request = blindRequestRepository.findById(blindRequestId).orElseThrow(()
                -> new TingApplicationException(ErrorCode.REQUEST_NOT_FOUND, ServiceType.BLIND));
        request.setStatus(RequestStatus.ACCEPTED);

        blindRequestRepository.save(request);
    }

    /**
     * 자신에게 온 요청 거절 -> 추가 구현 필요
     */
    public void rejectRequest(long blindRequestId) {
        BlindRequest request = blindRequestRepository.findById(blindRequestId).orElseThrow(() ->
                new TingApplicationException(ErrorCode.REQUEST_NOT_FOUND, ServiceType.BLIND));
        request.setStatus(RequestStatus.REJECTED);

        blindRequestRepository.save(request);
    }
}
