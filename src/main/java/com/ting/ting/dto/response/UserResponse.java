package com.ting.ting.dto.response;

import com.ting.ting.domain.User;
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

    private UserResponse(Long id, String username, String email, String major, Gender gender, LocalDate birth, MBTI mbti, Float weight, Float height, String idealPhoto) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.major = major;
        this.gender = gender;
        this.birth = birth;
        this.mbti = mbti;
        this.weight = weight;
        this.height = height;
        this.idealPhoto = idealPhoto;
    }

    public static UserResponse from(User entity) {
        return new UserResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getMajor(),
                entity.getGender(),
                entity.getBirth(),
                entity.getMbti(),
                entity.getWeight(),
                entity.getHeight(),
                entity.getIdealPhoto()
        );
    }
}
