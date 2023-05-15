package com.ting.ting.dto.response;

import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.MBTI;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String major;
    private Gender gender;
    private LocalDate birth;
    private MBTI mbti;
    private Float weight;
    private Float height;
    private String idealPhoto;

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
