package com.ting.ting.dto.response;

import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.LikeStatus;
import com.ting.ting.domain.constant.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BlindUserWithRequestStatusAndLikeStatusResponse {

    private BlindDateResponse blindDateResponse;
    private RequestStatus requestStatus;
    private LikeStatus likeStatus;

    public static BlindUserWithRequestStatusAndLikeStatusResponse of(User entity, RequestStatus requestStatus, LikeStatus likeStatus) {
        return new BlindUserWithRequestStatusAndLikeStatusResponse(
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
