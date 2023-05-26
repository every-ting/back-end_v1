package com.ting.ting.service;

import com.ting.ting.dto.response.GroupInvitationResponse;
import com.ting.ting.dto.response.GroupMemberResponse;

import java.util.Set;

public interface GroupInvitationService {

    /**
     * 팀장 - 했던 팀 초대 모두 조회
     */
    Set<GroupInvitationResponse> findAllGroupMemberInvitation(long groupId, long userIdOfLeader);

    /**
     * 팀장 - 팀 멤버로 초대(초대하는 QR 코드 생성)
     */
    GroupInvitationResponse createGroupMemberInvitation(long groupId, long userIdOfLeader);

    /**
     * 팀장 - 팀 멤버 초대 취소
     */
    void deleteGroupMemberInvitation(long groupId, long userIdOfLeader, long groupInvitationId);

    /**
     * 팀 초대 수락
     */
    GroupMemberResponse acceptGroupMemberInvitation(long groupId, long userId, String invitationCode);

    /**
     * 만료된 초대 코드를 삭제
     */
    void cleanupExpiredInvitations();
}
