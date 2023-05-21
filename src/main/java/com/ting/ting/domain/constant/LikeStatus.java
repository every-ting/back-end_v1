package com.ting.ting.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor

@Getter
public enum LikeStatus {
    DOING("찜 O"),
    NOTING("찜 X");

    private final String status;
}
