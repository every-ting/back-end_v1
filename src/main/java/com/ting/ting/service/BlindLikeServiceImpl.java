package com.ting.ting.service;

import com.ting.ting.domain.BlindDate;
import com.ting.ting.domain.BlindLike;
import com.ting.ting.domain.BlindRequest;
import com.ting.ting.domain.User;
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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

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
    public void createJoinLiked(long fromUserId, long toUserId) {
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
    public void deleteLikedByFromUserIdAndToUserId(long userId, long toUserId) {
        BlindLike request = blindLikeRepository.findByFromUser_IdAndToUser_Id(userId, toUserId)
                .orElseThrow(() -> throwException(ErrorCode.REQUEST_NOT_FOUND));
        blindLikeRepository.delete(request);
    }

    @Override
    public Set<BlindLikeResponse> getBlindLike(long userId) {
        User user = getUserById(userId);
        Set<BlindLike> allLikedUserInfos = blindLikeRepository.findAllByFromUser(user);
        Set<BlindDateResponse> blindDateResponses = new LinkedHashSet<>();

        for (BlindLike blindLikeInfo : allLikedUserInfos) {
            User toUser = blindLikeInfo.getToUser();
            blindDateResponses.add(BlindDateResponse.from(toUser));
        }

        Set<BlindLikeResponse> blindLikeResponses = new LinkedHashSet<>();

        Set<Long> myRequestPendingUsersId = getMyRequestPendingUsersId(user);

        Set<Long> userIdOfMeAndMyDateMatchedUsers = getUserIdOfMyDateMatchedUsers(user);

        for (BlindDateResponse blindDateResponse : blindDateResponses) {
            Long toUserId = blindDateResponse.getId();

            if (myRequestPendingUsersId.contains(toUserId)) {
                blindLikeResponses.add(BlindLikeResponse.of(blindDateResponse, RequestStatus.PENDING));
            } else if (userIdOfMeAndMyDateMatchedUsers.contains(toUserId)) {
                blindLikeResponses.add(BlindLikeResponse.of(blindDateResponse, null));
            } else {
                blindLikeResponses.add(BlindLikeResponse.of(blindDateResponse, RequestStatus.EMPTY));
            }
        }

        return blindLikeResponses;
    }

    private User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                throwException(ErrorCode.USER_NOT_FOUND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", userId)));
    }

    private Set<Long> getMyRequestPendingUsersId(User user) {
        Set<Long> myRequestPendingUsersId = new HashSet<>();

        Set<BlindRequest> myRequestPendingUsersInfo = blindRequestRepository.findAllByFromUserAndStatus(user, RequestStatus.PENDING);

        for (BlindRequest blindRequest : myRequestPendingUsersInfo) {
            myRequestPendingUsersId.add(blindRequest.getToUser().getId());
        }

        return myRequestPendingUsersId;
    }

    private Set<Long> getUserIdOfMyDateMatchedUsers(User user) {
        Set<Long> blindDateMatchedUsedId = new HashSet<>();

        Set<BlindDate> matchedUsers = blindDateRepository.getByMyMatchedUsers(user);

        for (BlindDate blindDate : matchedUsers) {
            blindDateMatchedUsedId.add(blindDate.getMenUser().getId());
            blindDateMatchedUsedId.add(blindDate.getWomenUser().getId());
        }

        return blindDateMatchedUsedId;
    }
}
