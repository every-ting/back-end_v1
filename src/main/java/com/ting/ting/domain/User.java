package com.ting.ting.domain;

import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.MBTI;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Table(name = "\"user\"", indexes = {
        @Index(columnList = "username")
})
@SQLDelete(sql = "UPDATE \"user\" SET deleted_at = NOW() where id=?")
@Where(clause = "deleted_at is NULL")
@Entity
public class User extends AuditingFields {

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

    @NotNull
    @Column(nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull
    @Column(nullable = false)
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(length = 4)
    private MBTI mbti;

    private Float weight;

    private Float height;

    @Column(name="ideal_photo")
    private String idealPhoto;

    @Column(name = "deleted_at")  // soft-delete
    private LocalDateTime deletedAt;

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

    public static User of(String username, String email, String school, String major, Gender gender, LocalDate birth) {
        return User.of(null, username, email, school, major, gender, birth);
    }

    public static User of(Long id, String username, String email, String school, String major, Gender gender, LocalDate birth) {
        return new User(id, username, email, school, major, gender, birth, null, null, null, null);
    }
}
