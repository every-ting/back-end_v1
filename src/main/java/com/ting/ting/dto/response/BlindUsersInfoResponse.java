package com.ting.ting.dto.response;

import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.MBTI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BlindUsersInfoResponse {

    private Long id;
    private String school;
    private String major;
    private MBTI mbti;
    private float weight;
    private float height;

    public static BlindUsersInfoResponse from(User entity) {
        return new BlindUsersInfoResponse(
                entity.getId(),
                entity.getSchool(),
                entity.getMajor(),
                entity.getMbti(),
                entity.getWeight(),
                entity.getHeight()
        );
    }
}
