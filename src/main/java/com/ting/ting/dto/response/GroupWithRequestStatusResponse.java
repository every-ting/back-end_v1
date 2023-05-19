package com.ting.ting.dto.response;

import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.domain.custom.SuggestedGroupWithRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GroupWithRequestStatusResponse {

    private GroupResponse group;
    private RequestStatus requestStatus;

    public static GroupWithRequestStatusResponse from(SuggestedGroupWithRequestStatus groupWithRequestStatus) {
        return new GroupWithRequestStatusResponse(
                new GroupResponse(
                        groupWithRequestStatus.getId(),
                        groupWithRequestStatus.getGroupName(),
                        groupWithRequestStatus.getGender(),
                        groupWithRequestStatus.getMemberSizeLimit(),
                        groupWithRequestStatus.getSchool(),
                        groupWithRequestStatus.isMatched(),
                        groupWithRequestStatus.isJoinable(),
                        groupWithRequestStatus.getMemo()
                ),
                groupWithRequestStatus.getRequestStatus()
        );
    }
}
