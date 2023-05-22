package com.ting.ting.dto.response;

import com.ting.ting.domain.constant.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BlindLikeResponse {

    private BlindDateResponse blindDateResponse;
    private RequestStatus requestStatus;

    public static BlindLikeResponse of(BlindDateResponse blindDateResponse, RequestStatus requestStatus) {
        return new BlindLikeResponse(
                blindDateResponse,
                requestStatus
        );
    }
}
