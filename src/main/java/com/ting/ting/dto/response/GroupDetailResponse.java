package com.ting.ting.dto.response;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.constant.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class GroupDetailResponse {

    private GroupResponse group;
    private Set<GroupMemberResponse> members;
    private MemberRole myRole;

    public static GroupDetailResponse from(Group entity, MemberRole role) {
        return new GroupDetailResponse(
                GroupResponse.from(entity),
                entity.getGroupMembers().stream().map(GroupMemberResponse::from).collect(Collectors.toUnmodifiableSet()),
                role
        );
    }
}
