package com.ting.ting.service;

import com.ting.ting.domain.BlindDate;
import com.ting.ting.domain.BlindLike;
import com.ting.ting.domain.BlindRequest;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.dto.response.BlindDateResponse;
import com.ting.ting.dto.response.BlindLikeResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.repository.BlindDateRepository;
import com.ting.ting.repository.BlindLikeRepository;
import com.ting.ting.repository.BlindRequestRepository;
import com.ting.ting.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BlindLikeServiceImpl extends AbstractService implements BlindLikeService {

    private final BlindLikeRepository blindLikeRepository;
    private final UserRepository userRepository;
    private final BlindRequestRepository blindRequestRepository;
    private final BlindDateRepository blindDateRepository;

    public BlindLikeServiceImpl(BlindLikeRepository blindLikeRepository, UserRepository userRepository, BlindRequestRepository blindRequestRepository, BlindDateRepository blindDateRepository) {
        super(ServiceType.BLIND);
        this.blindLikeRepository = blindLikeRepository;
        this.userRepository = userRepository;
        this.blindRequestRepository = blindRequestRepository;
        this.blindDateRepository = blindDateRepository;
    }

    @Override
    public Set<BlindLikeResponse> getBlindLike() {
        User user = getUserById(getCurrentUserId());

        Set<BlindDateResponse> blindDateResponses = blindLikeRepository.findAllByFromUser(user).stream().map(BlindLike::getToUser).map(BlindDateResponse::from).collect(Collectors.toUnmodifiableSet());

        Set<BlindLikeResponse> blindLikeResponses = new LinkedHashSet<>();

        Set<Long> myRequestPendingUsersId = getMyRequestPendingUsersId(user);

        Set<Long> userIdOfMeAndMyDateMatchedUsers = getUserIdOfMyDateMatchedUsers(user);

        for (BlindDateResponse blindDateResponse : blindDateResponses) {
            Long toUserId = blindDateResponse.getId();

            if (myRequestPendingUsersId.contains(toUserId)) {
                blindLikeResponses.add(BlindLikeResponse.of(blindDateResponse, RequestStatus.PENDING));
            } else if (userIdOfMeAndMyDateMatchedUsers.contains(toUserId)) {
                blindLikeResponses.add(BlindLikeResponse.of(blindDateResponse, RequestStatus.DISABLED));
            } else {
                blindLikeResponses.add(BlindLikeResponse.of(blindDateResponse, RequestStatus.EMPTY));
            }
        }

        return blindLikeResponses;
    }

    @Override
    public void createJoinLiked(long toUserId) {
        Long fromUserId = getCurrentUserId();

        if (fromUserId == toUserId) {
            throwException(ErrorCode.DUPLICATED_USER_REQUEST);
        }

        User fromUser = getUserById(fromUserId);

        User toUser = getUserById(toUserId);

        if (fromUser.getGender() == toUser.getGender()) {
            throwException(ErrorCode.GENDER_NOT_MATCH);
        }

        blindLikeRepository.findByFromUserAndToUser(fromUser, toUser).ifPresent(it -> {
            throwException(ErrorCode.DUPLICATED_REQUEST);
        });

        BlindLike request = new BlindLike();
        request.setFromUser(fromUser);
        request.setToUser(toUser);
        blindLikeRepository.save(request);
    }

    @Override
    public void deleteLikedByFromUserIdAndToUserId(long toUserId) {
        BlindLike request = blindLikeRepository.findByFromUser_IdAndToUser_Id(getCurrentUserId(), toUserId)
                .orElseThrow(() -> throwException(ErrorCode.REQUEST_NOT_FOUND));
        blindLikeRepository.delete(request);
    }

    private User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                throwException(ErrorCode.USER_NOT_FOUND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", userId)));
    }

    private Set<Long> getMyRequestPendingUsersId(User user) {
        Set<BlindRequest> myRequestPendingUsersInfo = blindRequestRepository.findAllByFromUserAndStatus(user, RequestStatus.PENDING);

        return myRequestPendingUsersInfo.stream().map(BlindRequest::getToUser).map(User::getId).collect(Collectors.toUnmodifiableSet());
    }

    private Set<Long> getUserIdOfMyDateMatchedUsers(User user) {
        Set<BlindDate> matchedUsers = blindDateRepository.getByMyMatchedUsers(user);

        if (user.getGender() == Gender.MEN) {
            return matchedUsers.stream().map(BlindDate::getWomenUser).map(User::getId).collect(Collectors.toSet());
        }
        return matchedUsers.stream().map(BlindDate::getMenUser).map(User::getId).collect(Collectors.toSet());
    }
}
