package com.ting.ting.dto.response;

import com.ting.ting.domain.constant.LikeStatus;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.domain.custom.GroupWithMemberCount;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GroupWithStatusResponse {

    private GroupResponse group;
    private RequestStatus requestStatus;
    private LikeStatus likeStatus;

    public static GroupWithStatusResponse from(GroupWithMemberCount groupWithMemberCount, RequestStatus requestStatus, LikeStatus likeStatus) {
        return new GroupWithStatusResponse(
                GroupResponse.from(groupWithMemberCount),
                requestStatus,
                likeStatus
        );
    }
}
