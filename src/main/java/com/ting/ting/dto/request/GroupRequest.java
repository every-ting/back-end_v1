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

    /**
     * 과팅 팀 이름
     */
    @NotNull @Size(min = 2, max = 20)
    String name;

    /**
     * 과팅 성별
     */
    @NotNull
    Gender gender;

    /**
     * 과팅 팀 멤버 수
     */
    @NotNull
    @Min(2) @Max(6)
    Integer numOfMember;

    /**
     * 과팅 팀의 학교
     */
    @NotNull
    String school;

    /**
     * 과팅 팀 소개
     */
    String memo;

    protected GroupRequest() {}

    public GroupDto toDto() {
        return GroupDto.of(name, gender, numOfMember, school, memo);
    }
}
