package com.ting.ting.domain.custom;

import com.ting.ting.domain.constant.MBTI;
import com.ting.ting.domain.constant.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BlindUserWithRequestStatus {

    private Long id;
    private String username;
    private String major;
    private MBTI mbti;
    private Float weight;
    private Float height;
    private String idealPhoto;
    private RequestStatus requestStatus;
}
