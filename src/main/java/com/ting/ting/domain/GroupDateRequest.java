package com.ting.ting.domain;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Table(name = "\"group_date_request\"", uniqueConstraints = {
        @UniqueConstraint(name = "unique_from_group_to_group", columnNames = {"from_id", "to_id"}),
})
@Entity
public class GroupDateRequest extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "from_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Group fromGroup;

    @JoinColumn(name = "to_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Group toGroup;

    protected GroupDateRequest() {}

    private GroupDateRequest(Group fromGroup, Group toGroup) {
        this.fromGroup = fromGroup;
        this.toGroup = toGroup;
    }

    public static GroupDateRequest of(Group fromGroup, Group toGroup) {
        return new GroupDateRequest(fromGroup, toGroup);
    }
}
