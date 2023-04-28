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
import java.util.LinkedHashSet;
import java.util.Set;

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

    private Float weight;

    private Float height;

    @Column(name="ideal_photo")
    private String idealPhoto;

    @ManyToMany(mappedBy = "members")
    private Set<Group> groups = new LinkedHashSet<>();

    protected User() {}

    private User(Long id, String username, String email, String school, String major, Gender gender, LocalDate birth, MBTI mbti, Float weight, Float height, String idealPhoto) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.school = school;
        this.major = major;
        this.gender = gender;
        this.birth = birth;
        this.mbti = mbti;
        this.weight = weight;
        this.height = height;
        this.idealPhoto = idealPhoto;
    }

    public static User of(Long id, String username, String email, String school, String major, Gender gender, LocalDate birth) {
        return new User(id, username, email, school, major, gender, birth, null, null, null, null);
    }
}
