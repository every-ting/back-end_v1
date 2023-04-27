package com.ting.ting.dto.request;

import com.ting.ting.domain.constant.Gender;
import com.ting.ting.dto.GroupDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@AllArgsConstructor
public class GroupRequest {

    @NotNull @Size(min = 2, max = 20)
    String name;

    @NotNull
    Gender gender;

    @NotNull
    @Min(2) @Max(6)
    Integer numOfMember;

    @NotNull
    String school;

    String memo;

    public GroupDto toDto() {
        return GroupDto.of(name, gender, numOfMember, school, memo);
    }
}
