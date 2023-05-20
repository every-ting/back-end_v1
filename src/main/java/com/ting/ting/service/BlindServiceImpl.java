package com.ting.ting.service;

import com.ting.ting.domain.BlindDate;
import com.ting.ting.domain.BlindRequest;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.dto.response.BlindDateResponse;
import com.ting.ting.dto.response.BlindRequestWithFromAndToResponse;
import com.ting.ting.dto.response.BlindUserWithRequestStatusResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.repository.BlindDateRepository;
import com.ting.ting.repository.BlindRequestRepository;
import com.ting.ting.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class BlindServiceImpl extends AbstractService implements BlindService {

    private final UserRepository userRepository;
    private final BlindRequestRepository blindRequestRepository;
    private final BlindDateRepository blindDateRepository;

    public BlindServiceImpl(UserRepository userRepository, BlindRequestRepository blindRequestRepository, BlindDateRepository blindDateRepository) {
        super(ServiceType.BLIND);
        this.userRepository = userRepository;
        this.blindRequestRepository = blindRequestRepository;
        this.blindDateRepository = blindDateRepository;
    }

    @Override
    public Page<BlindUserWithRequestStatusResponse> blindUsersInfo(Long userId, Pageable pageable) {
        User user = getUserById(userId);
        Set<Long> idToBeRemoved = getUserIdOfRequestToMeOrMyRequestNotPending(user);

        if (user.getGender() == Gender.MEN) {
            return getBlindUserWithRequestStatusResponses(user, pageable, userRepository.findAllByGenderAndIdNotIn(Gender.WOMEN, idToBeRemoved, pageable));
        }
        return getBlindUserWithRequestStatusResponses(user, pageable, userRepository.findAllByGenderAndIdNotIn(Gender.MEN, idToBeRemoved, pageable));
    }

    private Page<BlindUserWithRequestStatusResponse> getBlindUserWithRequestStatusResponses(User user, Pageable pageable, Page<User> otherUsers) {
        List<BlindUserWithRequestStatusResponse> blindUserWithRequestStatusResponses = new ArrayList<>();

        Set<User> myRequestPendingUsers = getMyRequestPendingUsers(user);

        for (User otherUser : otherUsers) {
            if (myRequestPendingUsers.contains(otherUser)) {
                blindUserWithRequestStatusResponses.add(BlindUserWithRequestStatusResponse.of(otherUser, RequestStatus.PENDING));
            } else {
                blindUserWithRequestStatusResponses.add(BlindUserWithRequestStatusResponse.of(otherUser, null));
            }
        }

        return new PageImpl<>(blindUserWithRequestStatusResponses, pageable, otherUsers.getTotalElements());
    }

    private Set<User> getMyRequestPendingUsers(User user) {
        Set<User> myRequestPendingUsers = new HashSet<>();

        Set<BlindRequest> myRequestPendingUsersInfo = blindRequestRepository.findAllByFromUserAndStatus(user, RequestStatus.PENDING);

        for (BlindRequest blindRequest : myRequestPendingUsersInfo) {
            myRequestPendingUsers.add(blindRequest.getToUser());
        }

        return myRequestPendingUsers;
    }

    private Set<Long> getUserIdOfRequestToMeOrMyRequestNotPending(User user) {
        Set<Long> idToBeRemoved = new HashSet<>();

        Set<BlindRequest> requestToMeUserInfo = blindRequestRepository.findAllByToUser(user);
        for (BlindRequest userInfo : requestToMeUserInfo) {
            idToBeRemoved.add(userInfo.getFromUser().getId());
        }

        Set<BlindRequest> myRequestUserNotPending = blindRequestRepository.findAllByFromUserAndStatusIsNot(user, RequestStatus.PENDING);
        for (BlindRequest userInfo : myRequestUserNotPending) {
            idToBeRemoved.add(userInfo.getToUser().getId());
        }

        return idToBeRemoved;
    }

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

        if (blindDateRepository.countByBlindDate(fromUser) >= 3) {
            throwException(ErrorCode.LIMIT_NUMBER_OF_BlIND_DATE);
        }

        User toUser = userRepository.findById(toUserId).orElseThrow(() ->
                throwException(ErrorCode.USER_NOT_FOUND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", toUserId)));

        blindRequestRepository.findByFromUserAndToUser(fromUser, toUser).ifPresent(it -> {
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
    public BlindRequestWithFromAndToResponse getBlindRequest(long userId) {
        return new BlindRequestWithFromAndToResponse(
                requestToMe(userId),
                myRequest(userId)
        );
    }

    private Set<BlindDateResponse> myRequest(long fromUserId) {
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

    private Set<BlindDateResponse> requestToMe(long toUserId) {
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
    public void blindRequestAcceptedOrRejected(long userId, long blindRequestId, RequestStatus requestStatus) {
        BlindRequest blindRequest = getBlindRequest(userId, blindRequestId);

        User user = blindRequest.getToUser();
        User blindRequestUser = blindRequest.getFromUser();

        if (user.getGender() == blindRequestUser.getGender()) {
            blindRequestRepository.delete(blindRequest);
            throwException(ErrorCode.GENDER_NOT_MATCH);
        }

        if (blindRequest.getStatus() != RequestStatus.PENDING) {
            throwException(ErrorCode.REQUEST_ALREADY_PROCESSED);
        }

        blindRequest.setStatus(requestStatus);

        Optional<BlindRequest> oppositeCase = blindRequestRepository.findByFromUserAndToUser(user, blindRequestUser);

        oppositeCase.ifPresent(otherBlindRequest -> {
            otherBlindRequest.setStatus(requestStatus);
            blindRequestRepository.save(otherBlindRequest);
        });

        if (requestStatus == RequestStatus.ACCEPTED) {
            blindDateRepository.save(BlindDate.from(blindRequest));
        }

        blindRequestRepository.save(blindRequest);
    }

    private BlindRequest getBlindRequest(long userId, long blindRequestId) {
        BlindRequest blindRequest = getBlindRequestById(blindRequestId);
        validateRequestToMe(userId, blindRequest);
        return blindRequest;
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
