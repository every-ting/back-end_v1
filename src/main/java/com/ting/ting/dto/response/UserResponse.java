package com.ting.ting.dto.response;

import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.MBTI;

import java.time.LocalDate;

public class UserResponse {

    Long id;
    String username;
    String email;
    String major;
    Gender gender;
    LocalDate birth;
    MBTI mbti;
    Float weight;
    Float height;
    String idealPhoto;
}
