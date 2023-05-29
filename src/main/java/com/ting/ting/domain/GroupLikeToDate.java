package com.ting.ting.domain;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Table(name = "\"group_like_to_date\"")
@Entity
public class GroupLikeToDate extends AuditingFields{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "to_group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Group toGroup;

    @JoinColumn(name = "from_group_member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GroupMember fromGroupMember;

    protected GroupLikeToDate() {}

    private GroupLikeToDate(Group toGroup, GroupMember fromGroupMember) {
        this.toGroup = toGroup;
        this.fromGroupMember = fromGroupMember;
    }

    public static GroupLikeToDate of(Group toGroup, GroupMember fromGroupMember) {
        return new GroupLikeToDate(toGroup, fromGroupMember);
    }
}
