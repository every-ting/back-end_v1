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
    UserDto leaderDto;
    String name;
    Gender gender;
    int numOfMember;
    String school;
    boolean isMatched;
    String memo;

    public static GroupDto of(String name, Gender gender, int limit, String school, String memo) {
        return new GroupDto(null, null, name, gender, limit, school, false, memo);
    }

    public Group toEntity(User leader) {
        return Group.of(leader, name, gender, school, numOfMember, memo);
    }
}
