package com.ting.ting.dto.response;

import com.ting.ting.domain.constant.Gender;
import com.ting.ting.dto.GroupDto;
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

    public static GroupResponse from(GroupDto dto) {
        return new GroupResponse(
                dto.getId(),
                dto.getGroupName(),
                dto.getGender(),
                dto.getNumOfMember(),
                dto.getSchool(),
                dto.isMatched(),
                dto.getMemo()
        );
    }
}
