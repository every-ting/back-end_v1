package com.ting.ting.dto.response;

import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.domain.custom.GroupWithMemberCount;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GroupWithRequestStatusResponse {

    private GroupResponse group;
    private RequestStatus requestStatus;

    public static GroupWithRequestStatusResponse from(GroupWithMemberCount groupWithMemberCount, RequestStatus requestStatus) {
        return new GroupWithRequestStatusResponse(
                GroupResponse.from(groupWithMemberCount),
                requestStatus
        );
    }
}
