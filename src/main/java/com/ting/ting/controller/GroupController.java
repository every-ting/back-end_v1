package com.ting.ting.controller;

import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.*;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequestMapping("/groups")
public interface GroupController {

    /**
     * 같은 성별 이면서 내가 속한 팀이 아닌 팀 조회
     */
    @GetMapping
    Response<Page<GroupWithRequestStatusResponse>> getSuggestedSameGenderGroupList(@ParameterObject Pageable pageable);

    /**
     * 내가 속한 팀 조회
     */
    @GetMapping("/my")
    Response<Set<GroupResponse>> myGroupList();

    /**
     * 팀 멤버 조회(팀장 포함)
     */
    @GetMapping("/{groupId}/members")
    Response<Set<GroupMemberResponse>> getGroupMemberList(@PathVariable Long groupId);

    /**
     * 그룹 생성
     */
    @PostMapping
    Response<GroupResponse> createGroup(@RequestBody GroupRequest request);

    /**
     * 같은 성별인 팀에 요청
     */
    @PostMapping("/request/{groupId}")
    Response<Void> sendJoinRequest(@PathVariable Long groupId);

    /**
     * 같은 성별인 팀에 했던 요청을 취소
     */
    @DeleteMapping("/request/{groupId}")
    Response<Void> deleteJoinRequest(@PathVariable Long groupId);

    /**
     * 팀 나가기
     */
    @DeleteMapping("/{groupId}/members")
    Response<Void> deleteGroupMember(@PathVariable Long groupId);

    /**
     * 팀장 - 했던 초대 모두 조회
     */
    @GetMapping("{groupId}/members/invitations")
    Response<Set<GroupInvitationResponse>> getGroupMemberInvitationList(@PathVariable Long groupId);

    /**
     * 팀장 - 과팅 팀 초대
     */
    @PostMapping("{groupId}/members/invitations")
    Response<GroupInvitationResponse> createGroupMemberInvitation(@PathVariable Long groupId);

    /**
     * 팀장 - 과팅 팀 초대 취소
     */
    @DeleteMapping("{groupId}/members/invitations/{groupInvitationId}")
    Response<Void> cancelGroupMemberInvitation(@PathVariable Long groupId, @PathVariable Long groupInvitationId);

    /**
     * 과팅 팀 초대 수락
     */
    // TODO : 일단 GetMapping으로 -> 프론트 url 정해지면 PutMapping으로 바꿀 예정.
    @GetMapping("{groupId}/members/invitations/{invitationCode}")
    Response<GroupMemberResponse> acceptGroupMemberInvitation(@PathVariable Long groupId, @PathVariable String invitationCode);

    /**
     * 팀장 - 팀장 넘기기
     */
    @PutMapping("/{groupId}/leader/{userIdOfNewLeader}")
    Response<Set<GroupMemberResponse>> changeGroupLeader(@PathVariable Long groupId, @PathVariable Long userIdOfNewLeader);

    /**
     * 팀장 - 팀 멤버 가입 요청 조회
     */
    @GetMapping("/{groupId}/members/requests")
    Response<Set<GroupMemberRequestResponse>> getMemberRequestToJoinMyGroup(@PathVariable Long groupId);

    /**
     * 팀장 - 팀 멤버 가입 요청 수락
     */
    @PostMapping("members/requests/{groupMemberRequestId}")
    Response<GroupMemberResponse> acceptJoinRequestToMyGroup(@PathVariable Long groupMemberRequestId);

    /**
     * 팀장 - 팀 멤버 가입 요청 거절
     */
    @DeleteMapping("members/requests/{groupMemberRequestId}")
    Response<Void> rejectJoinRequestToMyGroup(@PathVariable Long groupMemberRequestId);

    /**
     * 팀장 - 과팅 요청 조회(받은 요청, 한 요청 모두)
     */
    @GetMapping("/{groupId}/dates/requests")
    Response<GroupDateRequestWithFromAndToResponse> getGroupDateRequest(@PathVariable Long groupId);

    /**
     * 팀장 - 과팅 요청
     */
    @PostMapping("/{fromGroupId}/dates/requests/{toGroupId}")
    Response<GroupDateRequestResponse> saveGroupDateRequest(@PathVariable Long fromGroupId, @PathVariable Long toGroupId);

    /**
     * 팀장 - 과팅 요청 취소
     */
    @DeleteMapping("/{fromGroupId}/dates/requests/{toGroupId}")
    Response<Void> deleteGroupDateRequest(@PathVariable Long fromGroupId, @PathVariable Long toGroupId);

    /**
     * 팀장 - 과팅 요청 수락
     */
    @PostMapping("/dates/requests/{groupDateRequestId}")
    Response<GroupDateResponse> acceptGroupDateRequest(@PathVariable Long groupDateRequestId);

    /**
     * 팀장 - 과팅 요청 거절
     */
    @DeleteMapping("/dates/requests/{groupDateRequestId}")
    Response<Void> rejectGroupDateRequest(@PathVariable Long groupDateRequestId);
}
