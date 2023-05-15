package com.ting.ting.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MemberStatus {
    PENDING("멤버 초대 중"),
    ACTIVE("멤버 활동 중");

    private final String status;
}
