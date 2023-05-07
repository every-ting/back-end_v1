package com.ting.ting.dto.response;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GroupResponse {

    Long id;
    String groupName;
    Gender gender;
    int numOfMember;
    String school;
    boolean isMatched;
    String memo;

    public static GroupResponse from(Group entity) {
        return new GroupResponse(
                entity.getId(),
                entity.getGroupName(),
                entity.getGender(),
                entity.getNumOfMember(),
                entity.getSchool(),
                entity.isMatched(),
                entity.getMemo()
        );
    }
}
