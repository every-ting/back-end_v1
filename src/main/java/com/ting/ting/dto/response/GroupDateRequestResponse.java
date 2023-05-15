package com.ting.ting.dto.response;

import com.ting.ting.domain.GroupDateRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GroupDateRequestResponse {

    private Long id;
    private GroupResponse fromGroup;

    public static GroupDateRequestResponse from(GroupDateRequest entity) {
        return new GroupDateRequestResponse(
                entity.getId(),
                GroupResponse.from(entity.getFromGroup())
        );
    }
}
