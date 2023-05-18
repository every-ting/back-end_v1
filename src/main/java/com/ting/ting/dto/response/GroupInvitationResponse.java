package com.ting.ting.dto.response;

import com.ting.ting.domain.GroupInvitation;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GroupInvitationResponse {

    private String invitationQRImageUrl;

    public static GroupInvitationResponse from(GroupInvitation entity) {
        return new GroupInvitationResponse(entity.getQrImageUrl());
    }
}
