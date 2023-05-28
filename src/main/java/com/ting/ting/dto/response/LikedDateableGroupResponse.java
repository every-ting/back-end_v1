package com.ting.ting.dto.response;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.constant.LikeStatus;
import com.ting.ting.domain.constant.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LikedDateableGroupResponse {

    private GroupWithMemberInfoResponse group;
    private RequestStatus requestStatus;
    private LikeStatus likeStatus;
    private int likeCount;

    public static LikedDateableGroupResponse from(Group entity, RequestStatus requestStatus, LikeStatus likeStatus, int likeCount) {
        return new LikedDateableGroupResponse(
                GroupWithMemberInfoResponse.from(entity),
                requestStatus,
                likeStatus,
                likeCount
        );
    }
}
