package com.ting.ting.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RequestStatus {
    PENDING("요청중"),
    ACCEPTED("성공"),
    REJECTED("거절");

    private final String status;
}
