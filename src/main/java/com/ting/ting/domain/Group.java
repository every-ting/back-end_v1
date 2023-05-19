package com.ting.ting.domain;

import com.ting.ting.domain.constant.Gender;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Table(name = "\"group\"")
@Entity
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @NotNull @Size(min = 2, max = 20)
    @Column(name = "group_name", unique = true, nullable = false, length = 20)
    private String groupName;

    @NotNull
    @Column(nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull @Size(max = 70)
    @Column(length = 70, nullable = false)
    private String school;

    @Setter
    @NotNull @Min(2) @Max(6)
    @Column(name = "member_size_limit", nullable = false)
    private int memberSizeLimit;

    @Setter
    @NotNull
    @Column(name = "is_matched", nullable = false)
    private boolean isMatched = false;

    @Setter
    @NotNull
    @Column(name = "is_joinable", nullable = false)
    private boolean isJoinable = true;

    private String memo;

    protected Group() {}

    private Group(String groupName, Gender gender, String school, int memberSizeLimit, String memo) {
        this.groupName = groupName;
        this.gender = gender;
        this.school = school;
        this.memberSizeLimit = memberSizeLimit;
        this.memo = memo;
    }

    public static Group of(String groupName, Gender gender, String school, int memberSizeLimit, String memo) {
        return new Group(groupName, gender, school, memberSizeLimit, memo);
    }
}
