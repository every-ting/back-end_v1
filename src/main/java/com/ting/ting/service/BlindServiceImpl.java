package com.ting.ting.service;

import com.ting.ting.domain.BlindDate;
import com.ting.ting.domain.BlindLike;
import com.ting.ting.domain.BlindRequest;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.LikeStatus;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.dto.response.BlindDateResponse;
import com.ting.ting.dto.response.BlindRequestResponse;
import com.ting.ting.dto.response.BlindRequestWithFromAndToResponse;
import com.ting.ting.dto.response.BlindUserWithRequestStatusAndLikeStatusResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.repository.BlindDateRepository;
import com.ting.ting.repository.BlindLikeRepository;
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
    private final BlindLikeRepository blindLikeRepository;

    public BlindServiceImpl(UserRepository userRepository, BlindRequestRepository blindRequestRepository, BlindDateRepository blindDateRepository, BlindLikeRepository blindLikeRepository) {
        super(ServiceType.BLIND);
        this.userRepository = userRepository;
        this.blindRequestRepository = blindRequestRepository;
        this.blindDateRepository = blindDateRepository;
        this.blindLikeRepository = blindLikeRepository;
    }

    @Override
    public Page<BlindUserWithRequestStatusAndLikeStatusResponse> blindUsersInfo(Long userId, Pageable pageable) {
        User user = getUserById(userId);
        Set<Long> idToBeRemoved = getUserIdOfMyDateMatchedUsers(user);

        idToBeRemoved.add(userId);

        if (user.getGender() == Gender.MEN) {
            return getBlindUserWithRequestStatusAndLikeStatusResponses(user, pageable, userRepository.findAllByGenderAndIdNotIn(Gender.WOMEN, idToBeRemoved, pageable));
        }
        return getBlindUserWithRequestStatusAndLikeStatusResponses(user, pageable, userRepository.findAllByGenderAndIdNotIn(Gender.MEN, idToBeRemoved, pageable));
    }

    private Page<BlindUserWithRequestStatusAndLikeStatusResponse> getBlindUserWithRequestStatusAndLikeStatusResponses(User user, Pageable pageable, Page<User> otherUsers) {
        List<BlindUserWithRequestStatusAndLikeStatusResponse> blindUserWithRequestStatusAndLikeStatusResponses = new ArrayList<>();

        Set<User> myRequestPendingUsers = getMyRequestPendingUsers(user);

        Set<User> myLikedUsers = getMyLikedUser(user);

        for (User otherUser : otherUsers) {
            if (myRequestPendingUsers.contains(otherUser)) {
                checkLikedUserAndUpdateBlindUserList(blindUserWithRequestStatusAndLikeStatusResponses, myLikedUsers, otherUser, RequestStatus.PENDING);
            } else {
                checkLikedUserAndUpdateBlindUserList(blindUserWithRequestStatusAndLikeStatusResponses, myLikedUsers, otherUser, RequestStatus.EMPTY);
            }
        }
        return new PageImpl<>(blindUserWithRequestStatusAndLikeStatusResponses, pageable, otherUsers.getTotalElements());
    }

    private void checkLikedUserAndUpdateBlindUserList(List<BlindUserWithRequestStatusAndLikeStatusResponse> blindUserWithRequestStatusAndLikeStatusResponse, Set<User> myLikedUsers, User otherUser, RequestStatus requestStatus) {
        if (myLikedUsers.contains(otherUser)) {
            blindUserWithRequestStatusAndLikeStatusResponse.add(BlindUserWithRequestStatusAndLikeStatusResponse.of(otherUser, requestStatus, LikeStatus.LIKED));
        } else {
            blindUserWithRequestStatusAndLikeStatusResponse.add(BlindUserWithRequestStatusAndLikeStatusResponse.of(otherUser, requestStatus, LikeStatus.NOT_LIKED));
        }
    }

    private Set<Long> getUserIdOfMyDateMatchedUsers(User user) {
        Set<Long> idToBeRemoved = new HashSet<>();

        Set<BlindDate> matchedUsers = blindDateRepository.getByMyMatchedUsers(user);

        for (BlindDate blindDate : matchedUsers) {
            idToBeRemoved.add(blindDate.getMenUser().getId());
            idToBeRemoved.add(blindDate.getWomenUser().getId());
        }

        return idToBeRemoved;
    }

    @Override
    public void createJoinRequest(long fromUserId, long toUserId) {
        if (fromUserId == toUserId) {
            throwException(ErrorCode.DUPLICATED_USER_REQUEST);
        }

        User fromUser = getUserById(fromUserId);

        if (blindRequestRepository.countByFromUserAndStatus(fromUser, RequestStatus.PENDING) >= 5) {
            throwException(ErrorCode.LIMIT_NUMBER_OF_REQUEST);
        }

        if (blindDateRepository.countByBlindDate(fromUser) >= 3) {
            throwException(ErrorCode.LIMIT_NUMBER_OF_BlIND_DATE);
        }

        User toUser = getUserById(toUserId);

        if (fromUser.getGender() == toUser.getGender()) {
            throwException(ErrorCode.GENDER_NOT_MATCH);
        }

        blindRequestRepository.findByFromUserAndToUser(fromUser, toUser).ifPresent(it -> {
            throwException(ErrorCode.DUPLICATED_REQUEST);
        });

        BlindRequest request = new BlindRequest();
        request.setFromUser(fromUser);
        request.setToUser(toUser);
        blindRequestRepository.save(request);
    }

    @Override
    public void deleteRequestById(long userId, long toUserId) {
        BlindRequest request = blindRequestRepository.findByFromUser_IdAndToUser_Id(userId, toUserId)
                .orElseThrow(() -> throwException(ErrorCode.REQUEST_NOT_FOUND));
        blindRequestRepository.delete(request);
    }

    @Override
    public BlindRequestWithFromAndToResponse getBlindRequest(long userId) {
        return new BlindRequestWithFromAndToResponse(
                getBlindRequestResponseByBlindDateResponse(userId, requestToMe(userId)),
                getBlindRequestResponseByBlindDateResponse(userId, myRequest(userId))
        );
    }

    private Set<BlindRequestResponse> getBlindRequestResponseByBlindDateResponse(long userId, Set<BlindDateResponse> blindDateResponses) {
        User user = getUserById(userId);

        Set<BlindRequestResponse> blindRequestResponses = new LinkedHashSet<>();

        Set<Long> blindLikedUserId = getMyLikedUserId(user);

        for (BlindDateResponse blindDateResponse : blindDateResponses) {
            long toUserId = blindDateResponse.getId();

            if (blindLikedUserId.contains(toUserId)) {
                blindRequestResponses.add(BlindRequestResponse.of(blindDateResponse, LikeStatus.LIKED));
            } else {
                blindRequestResponses.add(BlindRequestResponse.of(blindDateResponse, LikeStatus.NOT_LIKED));
            }
        }

        return blindRequestResponses;
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

    @Override
    public void acceptRequest(long userId, long blindRequestId) {
        BlindRequest blindRequest = getBlindRequestById(blindRequestId);

        if (blindRequest.getToUser().getId() != userId) {
            throwException(ErrorCode.REQUEST_NOT_MINE);
        }

        User user = blindRequest.getToUser();
        User blindRequestUser = blindRequest.getFromUser();

        if (user.getGender() == blindRequestUser.getGender()) {
            blindRequestRepository.delete(blindRequest);
            throwException(ErrorCode.GENDER_NOT_MATCH);
        }

        if (blindRequest.getStatus() != RequestStatus.PENDING) {
            throwException(ErrorCode.REQUEST_ALREADY_PROCESSED);
        }

        blindRequest.setStatus(RequestStatus.ACCEPTED);

        Optional<BlindRequest> oppositeCase = blindRequestRepository.findByFromUserAndToUser(user, blindRequestUser);

        oppositeCase.ifPresent(otherBlindRequest -> {
            if (otherBlindRequest.getStatus() == RequestStatus.ACCEPTED) {
                throwException(ErrorCode.REQUEST_ALREADY_PROCESSED);
            }

            otherBlindRequest.setStatus(RequestStatus.ACCEPTED);
            blindRequestRepository.save(otherBlindRequest);
        });

        blindDateRepository.save(BlindDate.from(blindRequest));
        blindRequestRepository.save(blindRequest);
    }

    @Override
    public void rejectRequest(long userId, long blindRequestId) {
        BlindRequest blindRequest = getBlindRequestById(blindRequestId);

        if (blindRequest.getToUser().getId() != userId) {
            throwException(ErrorCode.REQUEST_NOT_MINE);
        }

        blindRequestRepository.delete(blindRequest);
    }

    private User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                throwException(ErrorCode.USER_NOT_FOUND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", userId)));
    }

    private BlindRequest getBlindRequestById(long blindRequestId) {
        return blindRequestRepository.findById(blindRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND));
    }

    private Set<User> getMyRequestPendingUsers(User user) {
        Set<User> myRequestPendingUsers = new HashSet<>();

        Set<BlindRequest> myRequestPendingUsersInfo = blindRequestRepository.findAllByFromUserAndStatus(user, RequestStatus.PENDING);

        for (BlindRequest blindRequest : myRequestPendingUsersInfo) {
            myRequestPendingUsers.add(blindRequest.getToUser());
        }

        return myRequestPendingUsers;
    }

    private Set<User> getMyLikedUser(User user) {
        Set<User> myLikedUser = new HashSet<>();

        Set<BlindLike> myLikedUserInfo = blindLikeRepository.findAllByFromUser(user);

        for (BlindLike blindLike : myLikedUserInfo) {
            myLikedUser.add(blindLike.getToUser());
        }

        return myLikedUser;
    }

    private Set<Long> getMyLikedUserId(User user) {
        Set<Long> myLikedUserId = new HashSet<>();

        Set<BlindLike> myLikedUserInfo = blindLikeRepository.findAllByFromUser(user);

        for (BlindLike blindLike : myLikedUserInfo) {
            myLikedUserId.add(blindLike.getToUser().getId());
        }

        return myLikedUserId;
    }
}
