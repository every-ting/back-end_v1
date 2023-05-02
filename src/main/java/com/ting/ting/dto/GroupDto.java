package com.ting.ting.dto;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GroupDto {

    Long id;
    String groupName;
    Gender gender;
    int numOfMember;
    String school;
    boolean isMatched;
    String memo;

    public static GroupDto of(String groupName, Gender gender, int limit, String school, String memo) {
        return new GroupDto(null, groupName, gender, limit, school, false, memo);
    }

    public Group toEntity(User leader) {
        return Group.of(leader, groupName, gender, school, numOfMember, memo);
    }

    public static GroupDto from(Group entity) {
        return new GroupDto(
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
