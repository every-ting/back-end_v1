package com.ting.ting.dto.response;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.custom.GroupWithMemberCount;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GroupResponse {

    private Long id;
    private String groupName;
    private Gender gender;
    private Integer memberCount;
    private int memberSizeLimit;
    private String school;
    private boolean isMatched;
    private boolean isJoinable;
    private String memo;

    public static GroupResponse from(Group entity) {
        return new GroupResponse(
                entity.getId(),
                entity.getGroupName(),
                entity.getGender(),
                null,
                entity.getMemberSizeLimit(),
                entity.getSchool(),
                entity.isMatched(),
                entity.isJoinable(),
                entity.getMemo()
        );
    }

    public static GroupResponse from(GroupWithMemberCount entity) {
        return new GroupResponse(
                entity.getId(),
                entity.getGroupName(),
                entity.getGender(),
                Math.toIntExact(entity.getMemberCount()),
                entity.getMemberSizeLimit(),
                entity.getSchool(),
                entity.isMatched(),
                entity.isJoinable(),
                entity.getMemo()
        );
    }
}
