package com.ting.ting.domain;

import lombok.Getter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Getter
@Table(name = "\"group_date_request\"", uniqueConstraints = {
        @UniqueConstraint(name = "unique_from_group_to_group", columnNames = {"from_id", "to_id"}),
})
@Entity
public class GroupDateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @JoinColumn(name = "from_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Group fromGroup;

    @NotNull
    @JoinColumn(name = "to_id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Group toGroup;

    protected GroupDateRequest() {}

    private GroupDateRequest(Group fromGroup, Group toGroup) {
        this.fromGroup = fromGroup;
        this.toGroup = toGroup;
    }

    public static GroupDateRequest of(Group fromGroup, Group toGroup) {
        return new GroupDateRequest(fromGroup, toGroup);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupDateRequest)) return false;
        GroupDateRequest that = (GroupDateRequest) o;
        return this.getId() != null && this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() { return Objects.hash(this.getId()); }
}
