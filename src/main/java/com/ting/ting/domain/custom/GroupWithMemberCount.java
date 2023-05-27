package com.ting.ting.domain.custom;

import com.ting.ting.domain.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GroupWithMemberCount {

    private Long id;
    private String groupName;
    private Gender gender;
    private Long memberCount;
    private int memberSizeLimit;
    private String school;
    private boolean isMatched;
    private boolean isJoinable;
    private String memo;
    private LocalDateTime createdAt;
}
