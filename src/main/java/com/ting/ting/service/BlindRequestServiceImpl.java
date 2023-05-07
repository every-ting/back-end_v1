package com.ting.ting.service;

import com.ting.ting.domain.BlindRequest;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.dto.response.BlindRequestResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.exception.TingApplicationException;
import com.ting.ting.repository.BlindRequestRepository;
import com.ting.ting.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BlindRequestServiceImpl extends AbstractService implements BlindRequestService {

    private final UserRepository userRepository;
    private final BlindRequestRepository blindRequestRepository;

    public BlindRequestServiceImpl(UserRepository userRepository, BlindRequestRepository blindRequestRepository) {
        super(ServiceType.BLIND);
        this.userRepository = userRepository;
        this.blindRequestRepository = blindRequestRepository;
    }

    @Override
    public Page<BlindRequestResponse> blindUsersInfo(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new TingApplicationException(ErrorCode.USER_NOT_FOUND, ServiceType.BLIND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", userId)));

        if (user.getGender().equals(Gender.MEN)) {
            return womenBlindUsersInfo(pageable);
        }

        return menBlindUsersInfo(pageable);
    }

    private Page<BlindRequestResponse> womenBlindUsersInfo(Pageable pageable) {
        return userRepository.findAllByGender(Gender.WOMEN, pageable).map(BlindRequestResponse::from);
    }

    private Page<BlindRequestResponse> menBlindUsersInfo(Pageable pageable) {
        return userRepository.findAllByGender(Gender.MEN, pageable).map(BlindRequestResponse::from);
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
        blindRequestRepository.save(request);
    }

    @Override
    public void deleteRequest(long blindRequestId) {
        BlindRequest request = blindRequestRepository.findById(blindRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND));

        blindRequestRepository.delete(request);
    }

    @Override
    public Set<BlindRequestResponse> myRequest(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                throwException(ErrorCode.USER_NOT_FOUND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", userId)));

        Set<BlindRequest> usersOfRequestedInfo = blindRequestRepository.findAllByFromUser(user);

        LinkedHashSet<User> usersOfRequested = new LinkedHashSet<>();

        for (BlindRequest requestToMeUserInfo : usersOfRequestedInfo) {
            Long toUserId = requestToMeUserInfo.getToUser().getId();
            User toUser = userRepository.findById(toUserId).orElseThrow(() ->
                    throwException(ErrorCode.USER_NOT_FOUND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", requestToMeUserInfo.getId())));

            usersOfRequested.add(toUser);
        }

        return usersOfRequested.stream().map(BlindRequestResponse::from).collect(Collectors.toCollection(LinkedHashSet::new));
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
