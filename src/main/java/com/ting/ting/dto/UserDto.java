package com.ting.ting.dto;

import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.MBTI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class UserDto {

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

    public static UserDto from(User entity) {
        return new UserDto(
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
