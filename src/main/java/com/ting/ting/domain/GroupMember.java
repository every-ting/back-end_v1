package com.ting.ting.domain;

import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.MemberStatus;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Table(name = "\"group_member\"")
@FilterDef(name = "validGroupMemberFilter", parameters = @ParamDef(name = "now", type = "java.time.LocalDateTime"))
@Filter(name = "validGroupMemberFilter", condition = "expired_at > :now or expired_at is null")
@Entity
public class GroupMember extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @JoinColumn(name = "group_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Group group;

    @Setter
    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
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

    @Setter
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    protected GroupMember() {}

    private GroupMember(Group group, User user, MemberStatus status, MemberRole role) {
        this.group = group;
        this.member = user;
        this.status = status;
        this.role = role;
    }

    public static GroupMember of(Group group, User user, MemberStatus status, MemberRole role) {
        return new GroupMember(group, user, status, role);
    }
}
