package com.ting.ting.dto.response;

import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.MBTI;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BlindUsersInfoResponse {

    private Long id;
    private String school;
    private String major;
    private MBTI mbti;
    private float weight;
    private float height;

    public BlindUsersInfoResponse(User user) {
        this.id = user.getId();
        this.school = user.getSchool();
        this.major = user.getMajor();
        this.mbti = user.getMbti();
        this.weight = user.getWeight();
        this.height = user.getHeight();
    }
}
