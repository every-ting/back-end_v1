package com.ting.ting.dto.response;

import com.ting.ting.domain.constant.LikeStatus;
import com.ting.ting.domain.custom.GroupWithMemberCount;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GroupWithLikeStatusResponse {

    private GroupResponse group;
    private LikeStatus likeStatus;

    public static GroupWithLikeStatusResponse from(GroupWithMemberCount groupWithMemberCount, LikeStatus likeStatus) {
        return new GroupWithLikeStatusResponse(
                GroupResponse.from(groupWithMemberCount),
                likeStatus
        );
    }
}
