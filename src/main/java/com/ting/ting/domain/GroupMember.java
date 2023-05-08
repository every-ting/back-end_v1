package com.ting.ting.domain;

import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.MemberStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Table(name = "\"group_member\"", uniqueConstraints = {
        @UniqueConstraint(name = "unique_group_and_user", columnNames = {"group_id", "member_id"}),
})
@Entity
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @JoinColumn(name = "group_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Group group;

    @NotNull
    @JoinColumn(name = "member_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User member;

    @Setter
    @NotNull
    @Column(name = "status", length = 7)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Setter
    @NotNull
    @Column(name = "role", length = 6)
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    protected GroupMember() {}

    private GroupMember(Group group, User user, MemberStatus status, MemberRole role) {
        this.member = user;
        this.group = group;
        this.status = status;
        this.role = role;
    }

    public static GroupMember of(Group group, User user, MemberStatus status, MemberRole role) {
        return new GroupMember(group, user, status, role);
    }
}
