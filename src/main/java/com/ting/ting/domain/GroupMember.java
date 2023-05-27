package com.ting.ting.domain;

import com.ting.ting.domain.constant.MemberRole;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Table(name = "\"group_member\"", uniqueConstraints = {
        @UniqueConstraint(name = "unique_group_and_user", columnNames = {"group_id", "member_id"}),
})
@Entity
public class GroupMember extends AuditingFields {

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
    @Column(name = "role", length = 6)
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @OneToMany(mappedBy = "fromGroupMember", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private final Set<GroupLikeToDate> groupLikeToDates = new LinkedHashSet<>();

    protected GroupMember() {}

    private GroupMember(Group group, User user, MemberRole role) {
        this.group = group;
        this.member = user;
        this.role = role;
    }

    public static GroupMember of(Group group, User user, MemberRole role) {
        return new GroupMember(group, user, role);
    }
}
