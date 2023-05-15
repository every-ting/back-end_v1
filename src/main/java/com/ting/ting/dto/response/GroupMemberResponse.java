package com.ting.ting.dto.response;

import com.ting.ting.domain.GroupMember;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GroupMemberResponse {

    private UserResponse member;
    private MemberStatus status;
    private MemberRole role;

    public static GroupMemberResponse from(GroupMember entity) {
        return new GroupMemberResponse(UserResponse.from(entity.getMember()), entity.getStatus(), entity.getRole());
    }
}
