package com.ting.ting.service;

import com.ting.ting.domain.BlindRequest;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.dto.response.BlindDateResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.exception.TingApplicationException;
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

    public BlindRequestServiceImpl(UserRepository userRepository, BlindRequestRepository blindRequestRepository) {
        super(ServiceType.BLIND);
        this.userRepository = userRepository;
        this.blindRequestRepository = blindRequestRepository;
    }

    @Override
    public Page<BlindDateResponse> blindUsersInfo(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new TingApplicationException(ErrorCode.USER_NOT_FOUND, ServiceType.BLIND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", userId)));

        if (user.getGender().equals(Gender.MEN)) {
            return womenBlindUsersInfo(user, pageable);
        }

        return menBlindUsersInfo(user, pageable);
    }

    private Page<BlindDateResponse> womenBlindUsersInfo(User user, Pageable pageable) {
        List<BlindRequest> usersRelatedToMeInfo = blindRequestRepository.findAllByFromUserOrToUser(user, user);

        Set<Long> idOfUsersRelatedToMe = getIdOfUsersRelatedToMe(user, usersRelatedToMeInfo);

        return userRepository.findAllByGenderAndIdNotIn(Gender.WOMEN, idOfUsersRelatedToMe, pageable).map(BlindDateResponse::from);
    }

    private Page<BlindDateResponse> menBlindUsersInfo(User user, Pageable pageable) {
        List<BlindRequest> usersRelatedToMeInfo = blindRequestRepository.findAllByFromUserOrToUser(user, user);

        Set<Long> idOfUsersRelatedToMe = getIdOfUsersRelatedToMe(user, usersRelatedToMeInfo);

        return userRepository.findAllByGenderAndIdNotIn(Gender.MEN, idOfUsersRelatedToMe, pageable).map(BlindDateResponse::from);
    }

    private Set<Long> getIdOfUsersRelatedToMe(User user, List<BlindRequest> allByFromUserOrToUser) {
        Set<Long> idOfPeopleRelatedToMe = new HashSet<>();

        for (BlindRequest request : allByFromUserOrToUser) {
            idOfPeopleRelatedToMe.add(request.getToUser().getId());
            idOfPeopleRelatedToMe.add(request.getFromUser().getId());
        }
        idOfPeopleRelatedToMe.remove(user.getId());

        return idOfPeopleRelatedToMe;
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
        BlindRequest request = geBlindRequestById(blindRequestId);

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
        BlindRequest request = geBlindRequestById(blindRequestId);
        validateRequestToMe(userId, request);

        request.setStatus(RequestStatus.ACCEPTED);
        blindRequestRepository.save(request);
    }

    @Override
    public void rejectRequest(long userId, long blindRequestId) {
        BlindRequest request = geBlindRequestById(blindRequestId);
        validateRequestToMe(userId, request);

        request.setStatus(RequestStatus.REJECTED);
        blindRequestRepository.save(request);
    }

    private void validateRequestToMe(long userId, BlindRequest request) {
        if (request.getToUser().getId() != userId) {
            throwException(ErrorCode.REQUEST_NOT_MINE);
        }
    }

    private BlindRequest geBlindRequestById(long blindRequestId) {
        return blindRequestRepository.findById(blindRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND));
    }
}
