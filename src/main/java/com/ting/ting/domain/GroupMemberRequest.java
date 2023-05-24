package com.ting.ting.domain;

import com.ting.ting.domain.constant.RequestStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Setter
@Getter
@Table(name = "\"group_member_request\"", uniqueConstraints = {
        @UniqueConstraint(name = "unique_group_and_user", columnNames = {"group_id", "user_id"}),
})
@Entity
public class GroupMemberRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @JoinColumn(name = "group_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Group group;

    @NotNull
    @JoinColumn(name = "user_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    protected GroupMemberRequest() {}

    private GroupMemberRequest(Group group, User user) {
        this.user = user;
        this.group = group;
    }

    public static GroupMemberRequest of(Group group, User user) {
        return new GroupMemberRequest(group, user);
    }
}
