package com.ting.ting.dto.request;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GroupRequest {

    /**
     * 과팅 팀 이름
     */
    @NotNull @Size(min = 2, max = 20)
    String groupName;

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
    Integer memberSizeLimit;

    /**
     * 과팅 팀의 학교
     */
    @NotNull
    String school;

    /**
     * 과팅 팀 소개
     */
    String memo;

    public Group toEntity() {
        return Group.of(groupName, gender, school, memberSizeLimit, memo);
    }
}
