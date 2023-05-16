package com.ting.ting.domain.custom;

import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SuggestedGroupWithRequestStatus {

    private Long id;
    private String groupName;
    private Gender gender;
    private String school;
    private int numOfMember;
    private boolean isMatched;
    private boolean isJoinable;
    private String memo;
    private RequestStatus requestStatus;
}
