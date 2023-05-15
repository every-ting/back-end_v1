package com.ting.ting.dto.response;

import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.domain.custom.BlindUserWithRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BlindUserWithRequestStatusResponse {

    BlindDateResponse blindDateResponse;
    private RequestStatus requestStatus;

    public static BlindUserWithRequestStatusResponse from(BlindUserWithRequestStatus blindUserWithRequestStatus) {
        return new BlindUserWithRequestStatusResponse(
                new BlindDateResponse(
                        blindUserWithRequestStatus.getId(),
                        blindUserWithRequestStatus.getUsername(),
                        blindUserWithRequestStatus.getMajor(),
                        blindUserWithRequestStatus.getMbti(),
                        blindUserWithRequestStatus.getWeight(),
                        blindUserWithRequestStatus.getHeight(),
                        blindUserWithRequestStatus.getIdealPhoto()
                ),
                blindUserWithRequestStatus.getRequestStatus()
        );
    }

    public static BlindUserWithRequestStatusResponse of(User entity, RequestStatus requestStatus) {
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
                requestStatus
        );
    }
}
