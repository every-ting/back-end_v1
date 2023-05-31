package com.ting.ting.service;

import com.ting.ting.dto.response.GroupMemberRequestResponse;
import com.ting.ting.dto.response.GroupMemberResponse;
import com.ting.ting.dto.response.JoinableGroupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface GroupMemberService {

    /**
     * 같은 성별인 팀에 요청
     */
    void saveJoinRequest(long groupId, long userId);

    /**
     * 같은 성별인 팀에 했던 요청을 취소
     */
    void deleteJoinRequest(long groupId, long userId);

    /**
     * 팀 멤버에서 삭제(팀 나가기)
     */
    void deleteGroupMember(long groupId, long userId);

    /**
     * 팀장 - 팀장 넘기기
     */
    Set<GroupMemberResponse> changeGroupLeader(long groupId, long userIdOfLeader, long userIdOfNewLeader);

    /**
     * 유저가 과팅 팀에 했던 가입 요청 조회
     */
    Page<JoinableGroupResponse> findUserJoinRequestList(Long userId, Pageable pageable);

    /**
     * 팀 멤버 조회
     */
    Set<GroupMemberResponse> findGroupMemberList(Long groupId);

    /**
     * 팀장 - 팀에 온 멤버 가입 요청을 조회
     */
    Set<GroupMemberRequestResponse> findMemberJoinRequest(long groupId, long userIdOfLeader);

    /**
     * 팀장 - 팀에 온 멤버 가입 요청을 수락
     */
    GroupMemberResponse acceptMemberJoinRequest(long userIdOfLeader, long groupMemberRequestId);

    /**
     * 팀장 - 팀에 온 멤버 가입 요청을 거절
     */
    void rejectMemberJoinRequest(long userIdOfLeader, long groupMemberRequestId);
}
