package com.ting.ting.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ting.ting.domain.Group;
import com.ting.ting.domain.constant.LikeStatus;
import com.ting.ting.domain.constant.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@Getter
@Setter
@AllArgsConstructor
public class DateableGroupResponse {

    private GroupWithMemberInfoResponse group;
    private RequestStatus requestStatus;
    private LikeStatus likeStatus;
    private Integer likeCount;

    public static DateableGroupResponse from(Group entity, LikeStatus likeStatus) {
        return from(entity, null, likeStatus, null);
    }

    public static DateableGroupResponse from(Group entity, RequestStatus requestStatus, LikeStatus likeStatus, Integer likeCount) {
        return new DateableGroupResponse(
                GroupWithMemberInfoResponse.from(entity),
                requestStatus,
                likeStatus,
                likeCount
        );
    }
}
