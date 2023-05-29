package com.ting.ting.domain.custom;

import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.MemberRole;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GroupWithMemberCountAndRole {

    private GroupWithMemberCount groupWithMemberCount;
    private MemberRole role;

    public GroupWithMemberCountAndRole(Long id, String groupName, Gender gender, Long memberCount, int memberSizeLimit, String school, boolean isMatched, boolean isJoinable, String memo, MemberRole role, LocalDateTime createdAt) {
        this.groupWithMemberCount = new GroupWithMemberCount(id, groupName, gender, memberCount, memberSizeLimit, school, isMatched, isJoinable, memo, createdAt);
        this.role = role;
    }
}
