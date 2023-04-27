package com.ting.ting.dto;

import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.MBTI;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserDto {

    Long id;
    String username;
    String email;
    String major;
    Gender gender;
    LocalDate birth;
    MBTI mbti;
    float weight;
    float height;
    String idealPhoto;
}
