package com.ting.ting.domain.custom;

import lombok.Getter;

@Getter
public class GroupIdWithLikeCount {

    private Long groupId;
    private int likeCount;

    public GroupIdWithLikeCount(Long groupId, Long likeCount) {
        this.groupId = groupId;
        this.likeCount = likeCount.intValue();
    }
}
