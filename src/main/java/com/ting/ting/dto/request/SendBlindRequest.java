package com.ting.ting.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
public class SendBlindRequest {

    @NotNull
    private Long fromUserId;

    @NotNull
    private Long toUserId;
}
