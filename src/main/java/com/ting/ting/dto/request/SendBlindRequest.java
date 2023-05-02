package com.ting.ting.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SendBlindRequest {

    /**
     * 소개팅 상대 정보
     */
    @NotNull
    private Long toUserId;
}
