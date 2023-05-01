package com.ting.ting.domain;

import com.ting.ting.domain.constant.RequestStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Setter
@Getter
@Table(name = "\"group_member\"", uniqueConstraints = {
        @UniqueConstraint(name = "unique_group_and_user", columnNames = {"group_id", "user_id"}),
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
    @JoinColumn(name = "user_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @NotNull
    @Column(name = "request_status", nullable = false, columnDefinition = "varchar(8) default 'PENDING'")
    @Enumerated(EnumType.STRING)
    RequestStatus status = RequestStatus.PENDING;

    protected GroupMember() {}

    private GroupMember(Group group, User user) {
        this.user = user;
        this.group = group;
    }

    public static GroupMember of(Group group, User user) {
        return new GroupMember(group, user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupMember)) return false;
        GroupMember that = (GroupMember) o;
        return this.getUser() != null && this.getUser().getId().equals(that.getUser().getId())
                && this.getGroup() != null && this.getGroup().getId().equals(that.getGroup().getId());
    }

    @Override
    public int hashCode() { return Objects.hash(this.getId()); }
}
