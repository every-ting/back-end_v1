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

    @JoinColumn(name = "from_group_member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GroupMember fromGroupMember;

    @JoinColumn(name = "to_group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Group toGroup;

    protected GroupLikeToDate() {}

    private GroupLikeToDate(GroupMember fromGroupMember, Group toGroup) {
        this.fromGroupMember = fromGroupMember;
        this.toGroup = toGroup;
    }

    public static GroupLikeToDate of(GroupMember fromGroupMember, Group toGroup) {
        return new GroupLikeToDate(fromGroupMember, toGroup);
    }
}
