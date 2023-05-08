package com.ting.ting.service;

import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.GroupMemberRequestResponse;
import com.ting.ting.dto.response.GroupMemberResponse;
import com.ting.ting.dto.response.GroupResponse;
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
     * 팀장 넘기기
     */
    Set<GroupMemberResponse> changeGroupLeader(long groupId, long leaderId, long memberId);

    /**
     * 내가 팀장인 팀에 온 멤버 가입 요청을 조회
     */
    Set<GroupMemberRequestResponse> findMemberJoinRequest(long groupId, long leaderId);
}
