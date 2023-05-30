package com.ting.ting.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RequestStatus {
    DISABLED("비활성화"),
    EMPTY("요청 X"),
    PENDING("요청중"),
    ACCEPTED("성공");

    private final String status;
}
