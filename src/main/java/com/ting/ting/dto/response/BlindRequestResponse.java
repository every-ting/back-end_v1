package com.ting.ting.dto.response;

import com.ting.ting.domain.constant.LikeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BlindRequestResponse {

    private BlindDateResponse blindDateResponse;
    private LikeStatus likeStatus;

    public static BlindRequestResponse of(BlindDateResponse blindDateResponse, LikeStatus likeStatus) {
        return new BlindRequestResponse(
                blindDateResponse,
                likeStatus
        );
    }
}
