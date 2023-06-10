package com.ting.ting.dto.response;

import com.ting.ting.domain.constant.LikeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BlindRequestResponseWithLikeStatus {

    private BlindRequestResponse blindRequestResponse;
    private LikeStatus likeStatus;

    public static BlindRequestResponseWithLikeStatus of(BlindRequestResponse blindRequestResponse, LikeStatus likeStatus) {
        return new BlindRequestResponseWithLikeStatus(
                blindRequestResponse,
                likeStatus
        );
    }
}
