package com.ting.ting.dto.response;

import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.custom.GroupWithMemberCountAndRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MyGroupResponse {

    GroupResponse group;
    MemberRole role;

    public static MyGroupResponse from(GroupWithMemberCountAndRole groupWithMemberCountAndRole) {
        return new MyGroupResponse(
                GroupResponse.from(groupWithMemberCountAndRole.getGroupWithMemberCount()),
                groupWithMemberCountAndRole.getRole()
        );
    }
}
