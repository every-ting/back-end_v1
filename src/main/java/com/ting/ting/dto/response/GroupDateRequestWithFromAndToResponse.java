package com.ting.ting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
public class GroupDateRequestWithFromAndToResponse {

    private Set<GroupResponse> fromGroup;
    private Set<GroupResponse> toGroup;
}
