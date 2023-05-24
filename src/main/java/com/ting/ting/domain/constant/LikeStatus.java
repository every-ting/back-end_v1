package com.ting.ting.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum LikeStatus {
    LIKED("찜 O"),
    NOT_LIKED("찜 X");

    private final String status;
}
