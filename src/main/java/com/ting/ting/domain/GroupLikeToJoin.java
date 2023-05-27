package com.ting.ting.domain;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Table(name = "\"group_like_to_join\"", uniqueConstraints = {
        @UniqueConstraint(name = "unique_group_and_user", columnNames = {"from_user_id", "to_group_id"}),
})
@Entity
public class GroupLikeToJoin extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "from_user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User fromUser;

    @JoinColumn(name = "to_group_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Group toGroup;

    protected GroupLikeToJoin() {}

    private GroupLikeToJoin(User fromUser, Group toGroup) {
        this.fromUser = fromUser;
        this.toGroup = toGroup;
    }

    public static GroupLikeToJoin of(User fromUser, Group toGroup) {
        return new GroupLikeToJoin(fromUser, toGroup);
    }
}
