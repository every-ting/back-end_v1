package com.ting.ting.dto.response;

import com.ting.ting.domain.GroupInvitation;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GroupInvitationResponse {

    private Long id;
    private String invitationCode;
    private String invitationQRImageUrl;

    public static GroupInvitationResponse from(GroupInvitation entity) {
        return new GroupInvitationResponse(
                entity.getId(),
                entity.getInvitationCode(),
                entity.getQrImageUrl()
        );
    }
}
