package com.ting.ting.dto.response;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupMember;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class GroupDetailResponse {

    private GroupResponse group;
    private List<UserResponse> members;
    private MemberRole role;

    public static GroupDetailResponse from(Group entity, MemberRole role) {
        return new GroupDetailResponse(
                GroupResponse.from(entity),
                entity.getGroupMembers().stream().map(GroupMember::getMember).map(UserResponse::from).collect(Collectors.toUnmodifiableList()),
                role
        );
    }
}
