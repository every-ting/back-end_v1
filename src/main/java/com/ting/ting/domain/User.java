package com.ting.ting.domain;

import com.ting.ting.domain.constant.Gender;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
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

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Email
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 70, nullable = false)
    private String school;

    @Column(length = 50, nullable = false)
    private String major;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Gender gender;

    @NotNull
    private LocalDate birth;

    private float weight;

    private float height;

    @Column(name="ideal_photo")
    private String idealPhoto;
}
