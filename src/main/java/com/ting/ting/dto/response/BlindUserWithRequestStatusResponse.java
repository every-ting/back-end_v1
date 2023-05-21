package com.ting.ting.dto.response;

import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.LikeStatus;
import com.ting.ting.domain.constant.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BlindUserWithRequestStatusResponse {

    private BlindDateResponse blindDateResponse;
    private RequestStatus requestStatus;
    private LikeStatus likeStatus;

    public static BlindUserWithRequestStatusResponse of(User entity, RequestStatus requestStatus, LikeStatus likeStatus) {
        return new BlindUserWithRequestStatusResponse(
                new BlindDateResponse(
                        entity.getId(),
                        entity.getUsername(),
                        entity.getMajor(),
                        entity.getMbti(),
                        entity.getWeight(),
                        entity.getHeight(),
                        entity.getIdealPhoto()
                ),
                requestStatus,
                likeStatus
        );
    }
}
