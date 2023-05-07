package com.ting.ting.service;

import com.ting.ting.domain.BlindRequest;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.repository.BlindRequestRepository;
import com.ting.ting.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class BlindRequestServiceImpl extends AbstractService implements BlindRequestService{

    private final UserRepository userRepository;
    private final BlindRequestRepository blindRequestRepository;

    public BlindRequestServiceImpl(UserRepository userRepository, BlindRequestRepository blindRequestRepository) {
        super(ServiceType.BLIND);
        this.userRepository = userRepository;
        this.blindRequestRepository = blindRequestRepository;
    }

    @Override
    public void createJoinRequest(long fromUserId, long toUserId) {
        if (Objects.equals(fromUserId, toUserId)) {
            throwException(ErrorCode.DUPLICATED_USER_REQUEST);
        }

        User fromUser = userRepository.findById(fromUserId).orElseThrow(() ->
                throwException(ErrorCode.USER_NOT_FOUND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", fromUserId)));
        User toUser = userRepository.findById(toUserId).orElseThrow(() ->
                throwException(ErrorCode.USER_NOT_FOUND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", toUserId)));

        blindRequestRepository.findByFromUserAndToUser(fromUser, toUser).ifPresent(it -> {
            throwException(ErrorCode.DUPLICATED_REQUEST);
        });

        BlindRequest request = new BlindRequest();
        request.setFromUser(fromUser);
        request.setToUser(toUser);
        request.setStatus(RequestStatus.PENDING);
        blindRequestRepository.save(request);
    }

    @Override
    public void deleteRequest(long blindRequestId) {
        BlindRequest request = blindRequestRepository.findById(blindRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND));

        blindRequestRepository.delete(request);
    }

    @Override
    public void acceptRequest(long blindRequestId) {
        BlindRequest request = blindRequestRepository.findById(blindRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND));
        request.setStatus(RequestStatus.ACCEPTED);

        blindRequestRepository.save(request);
    }

    @Override
    public void rejectRequest(long blindRequestId) {
        BlindRequest request = blindRequestRepository.findById(blindRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND));
        request.setStatus(RequestStatus.REJECTED);

        blindRequestRepository.save(request);
    }
}
