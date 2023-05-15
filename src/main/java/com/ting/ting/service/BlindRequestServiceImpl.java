package com.ting.ting.service;

import com.ting.ting.domain.BlindDate;
import com.ting.ting.domain.BlindRequest;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.dto.response.BlindDateResponse;
import com.ting.ting.dto.response.BlindUserWithRequestStatusResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.exception.TingApplicationException;
import com.ting.ting.repository.BlindDateRepository;
import com.ting.ting.repository.BlindRequestRepository;
import com.ting.ting.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class BlindRequestServiceImpl extends AbstractService implements BlindRequestService {

    private final UserRepository userRepository;
    private final BlindRequestRepository blindRequestRepository;
    private final BlindDateRepository blindDateRepository;

    public BlindRequestServiceImpl(UserRepository userRepository, BlindRequestRepository blindRequestRepository, BlindDateRepository blindDateRepository) {
        super(ServiceType.BLIND);
        this.userRepository = userRepository;
        this.blindRequestRepository = blindRequestRepository;
        this.blindDateRepository = blindDateRepository;
    }

    //Todo :: 조회 두번째 방법 -> join문 사용 X 반복문을 통한 조회
    @Override
    public Set<BlindUserWithRequestStatusResponse> blindUsersInfo(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new TingApplicationException(ErrorCode.USER_NOT_FOUND, ServiceType.BLIND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", userId)));

        Set<Long> userIdOfRequestToMe = getUserIdOfRequestToMe(blindRequestRepository.findAllByToUser(user));

        if (user.getGender() == Gender.MEN) {
            return getBlindUserWithRequestStatusResponses(user, userRepository.findAllByGenderAndIdNotIn(Gender.WOMEN, userIdOfRequestToMe, pageable));
        }
        return getBlindUserWithRequestStatusResponses(user, userRepository.findAllByGenderAndIdNotIn(Gender.MEN, userIdOfRequestToMe, pageable));
    }

    private Set<BlindUserWithRequestStatusResponse> getBlindUserWithRequestStatusResponses(User user, Page<User> otherUsers) {
        Set<BlindUserWithRequestStatusResponse> blindUserWithRequestStatusResponse = new LinkedHashSet<>();

        for (User otherUser : otherUsers) {
            Optional<BlindRequest> blindRequestUserInfo = blindRequestRepository.findByFromUserAndToUser(user, otherUser);
            if (blindRequestUserInfo.isEmpty()) {
                blindUserWithRequestStatusResponse.add(BlindUserWithRequestStatusResponse.of(
                        otherUser,
                        null
                ));
            } else {
                blindUserWithRequestStatusResponse.add(BlindUserWithRequestStatusResponse.of(
                        otherUser,
                        blindRequestUserInfo.get().getStatus()
                ));
            }
        }
        return blindUserWithRequestStatusResponse;
    }

    private Set<Long> getUserIdOfRequestToMe(Set<BlindRequest> allByToUser) {
        Set<Long> userIdOfRequestToMe = new HashSet<>();
        for (BlindRequest user1 : allByToUser) {
            userIdOfRequestToMe.add(user1.getFromUser().getId());
        }
        return userIdOfRequestToMe;
    }

    // Todo :: 조회 첫번째 방법 -> join문 사용
//    @Override
//    public Page<BlindUserWithRequestStatusResponse> blindUsersInfo(Long userId, Pageable pageable) {
//        User user = userRepository.findById(userId).orElseThrow(() ->
//                new TingApplicationException(ErrorCode.USER_NOT_FOUND, ServiceType.BLIND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", userId)));
//
//        if (user.getGender() == Gender.MEN) {
//            return userRepository.findAllBlindUserWithRequestStatusByUserAndGender(user, Gender.WOMEN, pageable).map(BlindUserWithRequestStatusResponse::from);
//        }
//        return userRepository.findAllBlindUserWithRequestStatusByUserAndGender(user, Gender.MEN, pageable).map(BlindUserWithRequestStatusResponse::from);
//    }

    @Override
    public void createJoinRequest(long fromUserId, long toUserId) {
        if (Objects.equals(fromUserId, toUserId)) {
            throwException(ErrorCode.DUPLICATED_USER_REQUEST);
        }

        User fromUser = userRepository.findById(fromUserId).orElseThrow(() ->
                throwException(ErrorCode.USER_NOT_FOUND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", fromUserId)));

        if (blindRequestRepository.countByFromUserAndStatus(fromUser, RequestStatus.PENDING) >= 5) {
            throwException(ErrorCode.LIMIT_NUMBER_OF_REQUEST);
        }

        User toUser = userRepository.findById(toUserId).orElseThrow(() ->
                throwException(ErrorCode.USER_NOT_FOUND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", toUserId)));

        blindRequestRepository.findByFromUserAndToUser(fromUser, toUser).ifPresent(it -> {
            throwException(ErrorCode.DUPLICATED_REQUEST);
        });

        blindRequestRepository.findByFromUserAndToUser(toUser, fromUser).ifPresent(it -> {
            throwException(ErrorCode.DUPLICATED_REQUEST);
        });

        if (fromUser.getGender() == toUser.getGender()) {
            throwException(ErrorCode.GENDER_NOT_MATCH);
        }

        BlindRequest request = new BlindRequest();
        request.setFromUser(fromUser);
        request.setToUser(toUser);
        blindRequestRepository.save(request);
    }

    @Override
    public void deleteRequest(long blindRequestId) {
        BlindRequest request = getBlindRequestById(blindRequestId);

        blindRequestRepository.delete(request);
    }

    @Override
    public Set<BlindDateResponse> myRequest(long fromUserId) {
        User fromUser = getUserById(fromUserId);

        Set<BlindRequest> usersOfRequestedInfo = blindRequestRepository.findAllByFromUserAndStatus(fromUser, RequestStatus.PENDING);

        LinkedHashSet<User> usersOfRequested = new LinkedHashSet<>();

        for (BlindRequest requestToMeUserInfo : usersOfRequestedInfo) {
            Long toUserId = requestToMeUserInfo.getToUser().getId();
            User toUser = getUserById(toUserId);
            usersOfRequested.add(toUser);
        }

        return usersOfRequested.stream().map(BlindDateResponse::from).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<BlindDateResponse> requestToMe(long toUserId) {
        Set<BlindRequest> usersOfRequestedInfo = blindRequestRepository.findAllByToUserAndStatus(getUserById(toUserId), RequestStatus.PENDING);

        LinkedHashSet<User> usersOfRequested = new LinkedHashSet<>();

        for (BlindRequest requestToMeUserInfo : usersOfRequestedInfo) {
            Long fromUserId = requestToMeUserInfo.getFromUser().getId();
            usersOfRequested.add(getUserById(fromUserId));
        }

        return usersOfRequested.stream().map(BlindDateResponse::from).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                throwException(ErrorCode.USER_NOT_FOUND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", userId)));
    }

    @Override
    public void acceptRequest(long userId, long blindRequestId) {
        BlindRequest blindRequest = getBlindRequestById(blindRequestId);
        validateRequestToMe(userId, blindRequest);

        User user = blindRequest.getToUser();
        User blindRequestUser = blindRequest.getFromUser();

        if (user.getGender() == blindRequestUser.getGender()) {
            blindRequestRepository.delete(blindRequest);
            throwException(ErrorCode.GENDER_NOT_MATCH);
        }

        blindRequest.setStatus(RequestStatus.ACCEPTED);
        blindRequestRepository.save(blindRequest);

        blindDateRepository.save(BlindDate.from(blindRequest));
    }

    @Override
    public void rejectRequest(long userId, long blindRequestId) {
        BlindRequest request = getBlindRequestById(blindRequestId);
        validateRequestToMe(userId, request);

        request.setStatus(RequestStatus.REJECTED);
        blindRequestRepository.save(request);
    }

    private void validateRequestToMe(long userId, BlindRequest request) {
        if (request.getToUser().getId() != userId) {
            throwException(ErrorCode.REQUEST_NOT_MINE);
        }
    }

    private BlindRequest getBlindRequestById(long blindRequestId) {
        return blindRequestRepository.findById(blindRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND));
    }
}
