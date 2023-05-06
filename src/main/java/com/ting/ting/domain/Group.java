package com.ting.ting.domain;

import com.ting.ting.domain.constant.Gender;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Setter
@Getter
@Table(name = "\"group\"")
@Entity
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private User leader;

    @NotNull @Size(min = 2, max = 20)
    @Column(name = "group_name", unique = true, nullable = false, length = 20)
    private String groupName;

    @NotNull
    @Column(nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull @Max(70)
    @Column(length = 70, nullable = false)
    private String school;

    @NotNull @Min(2) @Max(6)
    @Column(name = "num_of_member", nullable = false)
    private int numOfMember;

    @NotNull
    @Column(name = "is_matched", nullable = false)
    private boolean isMatched = false;

    private String memo;

    protected Group() {}

    private Group(User leader, String groupName, Gender gender, String school, int numOfMember, String memo) {
        this.leader = leader;
        this.groupName = groupName;
        this.gender = gender;
        this.school = school;
        this.numOfMember = numOfMember;
        this.memo = memo;
    }

    public static Group of(User leader, String groupName, Gender gender, String school, int numOfMember, String memo) {
        return new Group(leader, groupName, gender, school, numOfMember, memo);
    }
}
