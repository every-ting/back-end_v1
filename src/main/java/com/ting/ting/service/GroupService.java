package com.ting.ting.service;

import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface GroupService {

    /**
     * 모든 팀 조회
     */
    Page<GroupResponse> findAllGroups(Pageable pageable);

    /**
     * 같은 성별 이면서 내가 속한 팀이 아닌 팀 조회
     */
    Page<GroupResponse> findSuggestedGroupList(Pageable pageable);

    /**
     * 내가 속한 팀 조회 - request status : ACCEPTED
     */
    Set<GroupResponse> findMyGroupList(Long userId);

    /**
     * 팀 멤버 조회
     */
    Set<GroupMemberResponse> findGroupMemberList(Long groupId);

    /**
     * 그룹 생성
     */
    GroupResponse saveGroup(Long userId, GroupRequest request);

    /**
     * 같은 성별인 팀에 요청
     */
    void saveJoinRequest(long groupId, long userId);

    /**
     * 같은 성별인 팀에 했던 요청을 취소
     */
    void deleteJoinRequest(long groupId, long userId);

    /**
     * 팀 멤버에서 삭제
     */
    void deleteGroupMember(long groupId, long userId);

    /**
     * 팀장 넘기기
     */
    Set<GroupMemberResponse> changeGroupLeader(long groupId, long userIdOfLeader, long userIdOfNewLeader);

    /**
     * 내가 팀장인 팀에 온 멤버 가입 요청을 조회
     */
    Set<GroupMemberRequestResponse> findMemberJoinRequest(long groupId, long userIdOfLeader);

    /**
     * 내가 팀장인 팀에 온 멤버 가입 요청을 수락
     */
    GroupMemberResponse acceptMemberJoinRequest(long userIdOfLeader, long groupMemberRequestId);

    /**
     * 내가 팀장인 팀에 온 멤버 가입 요청을 거절
     */
    void rejectMemberJoinRequest(long userIdOfLeader, long groupMemberRequestId);

    /**
     * 내가 팀장인 팀에 온 과팅 요청 조회
     */
    Set<GroupDateRequestResponse> findAllGroupDateRequest(long groupId, long userIdOfLeader);

    /**
     * 내가 팀장인 팀에 온 과팅 요청 수락
     */
    GroupDateResponse acceptGroupDateRequest(long userIdOfLeader, long groupDateRequestId);

    /**
     * 내가 팀장인 팀에 온 과팅 요청 거절
     */
    void rejectGroupDateRequest(long userIdOfLeader, long groupDateRequestId);
}
