package com.ting.ting.dto.response;

import com.ting.ting.domain.BlindRequest;
import com.ting.ting.domain.constant.MBTI;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BlindRequestResponse {

    private Long id;
    private Long userId;
    private String username;
    private String major;
    private MBTI mbti;
    private Float weight;
    private Float height;
    private String idealPhoto;

    public static BlindRequestResponse fromUserInfo(BlindRequest blindRequest) {
        return new BlindRequestResponse(
                blindRequest.getId(),
                blindRequest.getFromUser().getId(),
                blindRequest.getFromUser().getUsername(),
                blindRequest.getFromUser().getMajor(),
                blindRequest.getFromUser().getMbti(),
                blindRequest.getFromUser().getWeight(),
                blindRequest.getFromUser().getHeight(),
                blindRequest.getFromUser().getIdealPhoto()
        );
    }

    public static BlindRequestResponse toUserInfo(BlindRequest blindRequest) {
        return new BlindRequestResponse(
                blindRequest.getId(),
                blindRequest.getToUser().getId(),
                blindRequest.getToUser().getUsername(),
                blindRequest.getToUser().getMajor(),
                blindRequest.getToUser().getMbti(),
                blindRequest.getToUser().getWeight(),
                blindRequest.getToUser().getHeight(),
                blindRequest.getToUser().getIdealPhoto()
        );
    }
}
