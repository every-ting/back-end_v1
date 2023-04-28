package com.ting.ting.domain;

import com.ting.ting.domain.constant.Gender;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.LinkedHashSet;
import java.util.Set;

@Setter
@Getter
@Table(name = "\"group\"", uniqueConstraints = {
        @UniqueConstraint(name = "unique_user", columnNames = {"leader_id"}),
})
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
    @Column(name = "group_name", nullable = false, length = 20)
    private String groupName;

    @NotNull
    @Column(nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull @Size(max = 70)
    @Column(length = 70, nullable = false)
    private String school;

    @NotNull @Min(2) @Max(6)
    @Column(name = "num_of_member", nullable = false)
    private int numOfMember;

    @NotNull
    @Column(name = "is_matched", nullable = false)
    private boolean isMatched = false;

    private String memo;

    @JoinTable(
            name = "member_request",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.LAZY)
    private Set<User> joinRequests = new LinkedHashSet<>();

    @JoinTable(
            name = "group_member",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @ManyToMany(fetch = FetchType.LAZY)
    private Set<User> members = new LinkedHashSet<>();

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

    public void addJoinRequests(User user) { this.joinRequests.add(user); }

    public void removeJoinRequests(User user) { this.joinRequests.remove(user); }

    public void addMember(User user) {
        this.members.add(user);
        user.getGroups().add(this);
    }

    public void removeMember(User user) {
        this.members.remove(user);
        user.getGroups().remove(this);
    }
}

