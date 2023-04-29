package com.ting.ting.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RequestStatus {
    P("요청중"),
    S("성공"),
    R("거절");

    private final String status;
}
