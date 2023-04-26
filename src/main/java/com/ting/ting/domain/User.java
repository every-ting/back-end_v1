package com.ting.ting.domain;

import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.MBTI;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Setter
@Getter
@Table(name = "\"user\"", indexes = {
        @Index(columnList = "username")
})
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @Size(min = 4, max = 100)
    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Email @NotNull
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotNull @Size(max = 70)
    @Column(length = 70, nullable = false)
    private String school;

    @NotNull @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String major;

    @NotNull @Size(max = 1)
    @Column(nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull
    @Column(nullable = false)
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    private MBTI mbti;

    private float weight;

    private float height;

    @Column(name="ideal_photo")
    private String idealPhoto;
}
