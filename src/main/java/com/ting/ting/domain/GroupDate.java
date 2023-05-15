package com.ting.ting.domain;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Table(name = "\"group_date\"", uniqueConstraints = {
        @UniqueConstraint(name = "unique_men_group_women_group", columnNames = {"men_group_id", "women_group_id"}),
})
@Entity
public class GroupDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "men_group_id", unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private Group menGroup;

    @JoinColumn(name = "women_group_id", unique = true)
    @ManyToOne(fetch = FetchType.LAZY)
    private Group womenGroup;

    protected GroupDate() {}

    private GroupDate(Group menGroup, Group womenGroup) {
        this.menGroup = menGroup;
        this.womenGroup = womenGroup;
    }

    public static GroupDate of(Group menGroup, Group womenGroup) {
        return new GroupDate(menGroup, womenGroup);
    }
}
