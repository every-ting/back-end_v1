package com.ting.ting.dto.request;

import com.ting.ting.domain.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    @NotNull
    private String username;

    @NotNull
    @Email
    private String socialEmail;

    @NotNull
    @Email
    private String email;

    @NotNull
    private Gender gender;

    @NotNull
    private String school;

    @NotNull
    private String major;

    @NotNull
    private LocalDate birth;
}
