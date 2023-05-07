package com.ting.ting.dto.response;

import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.MBTI;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BlindRequestResponse {

    private Long id;
    private String username;
    private String major;
    private MBTI mbti;
    private Float weight;
    private Float height;
    private String idealPhoto;

    public static BlindRequestResponse from(User entity) {
        return new BlindRequestResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getMajor(),
                entity.getMbti(),
                entity.getWeight(),
                entity.getHeight(),
                entity.getIdealPhoto()
        );
    }
}
