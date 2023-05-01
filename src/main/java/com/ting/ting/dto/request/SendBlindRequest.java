package com.ting.ting.dto.request;

import com.ting.ting.exception.UserException;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Getter
public class SendBlindRequest {

    @NotNull
    private final Long fromUserId;

    @NotNull
    private final Long toUserId;

    public SendBlindRequest(Long fromUserId, Long toUserId) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        if (Objects.equals(fromUserId, toUserId)) {
            throw new UserException("같은 사용자간의 요청 처리입니다.");
        }
    }
}
