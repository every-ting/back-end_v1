package com.ting.ting.filter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FilterType {
    VALID_GROUP_MEMBER("validGroupMemberFilter", "now", "now"),
    VALID_GROUP_INVITATION("validGroupInvitationFilter", "now", "now");

    private final String name;
    private final String parameter;
    private final String value;
}
