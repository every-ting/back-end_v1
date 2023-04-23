package com.ting.ting.domain;

import com.ting.ting.domain.constant.Gender;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "\"user\"")
public class User {

    public User() {
    }

    public User(String username, String school, String major, Gender gender, Date birth, String email, float weight, float height, String ideal_photo) {
        this.username = username;
        this.school = school;
        this.major = major;
        this.gender = gender;
        this.birth = birth;
        this.email = email;
        this.weight = weight;
        this.height = height;
        this.ideal_photo = ideal_photo;
    }

    public User(Long id, String username, String school, String major, Gender gender, Date birth, String email) {
        this.id = id;
        this.username = username;
        this.school = school;
        this.major = major;
        this.gender = gender;
        this.birth = birth;
        this.email = email;
    }

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    private String username;

    @NotNull
    private String school;

    @NotNull
    private String major;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Gender gender;

    @NotNull
    private Date birth;

    @Email
    private String email;

    private float weight;

    private float height;

    private String ideal_photo;
}
