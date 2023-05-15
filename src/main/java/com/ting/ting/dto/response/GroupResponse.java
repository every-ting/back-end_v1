package com.ting.ting.dto.response;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GroupResponse {

    private Long id;
    private String groupName;
    private Gender gender;
    private int numOfMember;
    private String school;
    private boolean isMatched;
    private boolean isJoinable;
    private String memo;

    public static GroupResponse from(Group entity) {
        return new GroupResponse(
                entity.getId(),
                entity.getGroupName(),
                entity.getGender(),
                entity.getNumOfMember(),
                entity.getSchool(),
                entity.isMatched(),
                entity.isJoinable(),
                entity.getMemo()
        );
    }
}
