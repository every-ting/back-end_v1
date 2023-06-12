package com.ting.ting.controller;

import com.ting.ting.dto.request.GroupCreateRequest;
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
    Response<Page<JoinableGroupResponse>> getJoinableSameGenderGroupList(@ParameterObject Pageable pageable);

    /**
     * 내가 속한 과팅 팀 상세 조회
     */
    @GetMapping("/{groupId}")
    Response<GroupDetailResponse> getGroupDetail(@PathVariable Long groupId);

    /**
     * 다른 성별의 팀 조회
     */
    @GetMapping("/{groupId}/opposite-gender-groups")
    Response<Page<DateableGroupResponse>> getOppositeGenderGroupList(@PathVariable Long groupId, @ParameterObject Pageable pageable);

    /**
     * 내가 속한 팀 조회
     */
    @GetMapping("/my")
    Response<Set<MyGroupResponse>> myGroupList();

    /**
     * 유저 기준 - 찜한 같은 성별의 팀 목록 조회(팀 가입을 위한)
     */
    @GetMapping("/likes")
    Response<Page<JoinableGroupResponse>> getGroupLikeToJoinList(@ParameterObject Pageable pageable);

    /**
     * 팀 기준 - 찜한 목록 조회(과팅 요청을 위한)
     */
    @GetMapping("/{groupId}/likes")
    Response<Page<DateableGroupResponse>> getGroupLikeToDateList(@PathVariable Long groupId, @ParameterObject Pageable pageable);

    /**
     * 받은 과팅 요청 조회
     */
    @GetMapping("/{groupId}/dates/requests")
    Response<Page<DateableGroupResponse>> getGroupDateRequest(@PathVariable Long groupId, @ParameterObject Pageable pageable);

    /**
     * 팀 멤버 조회(팀장 포함)
     */
    @GetMapping("/{groupId}/members")
    Response<Set<GroupMemberResponse>> getGroupMemberList(@PathVariable Long groupId);

    /**
     * 과팅 가입 요청 조회
     */
    @GetMapping("/requests")
    Response<Page<JoinableGroupResponse>> getUserJoinRequestList(@ParameterObject Pageable pageable);

    /**
     * 팀장 - 팀 멤버 가입 요청 조회
     */
    @GetMapping("/{groupId}/members/requests")
    Response<Set<GroupMemberRequestResponse>> getMemberRequestToJoinMyGroup(@PathVariable Long groupId);

    /**
     * 같은 성별의 팀 찜하기
     */
    @PostMapping("/likes/{toGroupId}")
    Response<Void> createSameGenderGroupLike(@PathVariable Long toGroupId);

    /**
     * 다른 성별의 팀 찜하기
     */
    @PostMapping("/{fromGroupId}/likes/{toGroupId}")
    Response<Void> createOppositeGenderGroupLike(@PathVariable Long fromGroupId, @PathVariable Long toGroupId);

    /**
     * 그룹 생성
     */
    @PostMapping
    Response<GroupResponse> createGroup(@RequestBody GroupCreateRequest request);

    /**
     * 팀장 - 과팅 요청
     */
    @PostMapping("/{fromGroupId}/dates/requests/{toGroupId}")
    Response<GroupDateRequestResponse> saveGroupDateRequest(@PathVariable Long fromGroupId, @PathVariable Long toGroupId);

    /**
     * 팀장 - 과팅 요청 수락
     */
    @PostMapping("/dates/requests/{groupDateRequestId}")
    Response<GroupDateResponse> acceptGroupDateRequest(@PathVariable Long groupDateRequestId);

    /**
     * 같은 성별인 팀에 가입 요청
     */
    @PostMapping("/requests/{groupId}")
    Response<Void> sendJoinRequest(@PathVariable Long groupId);

    /**
     * 팀장 - 팀 멤버 가입 요청 수락
     */
    @PostMapping("/members/requests/{groupMemberRequestId}")
    Response<GroupMemberResponse> acceptJoinRequestToMyGroup(@PathVariable Long groupMemberRequestId);

    /**
     * 팀장 - 팀장 넘기기
     */
    @PutMapping("/{groupId}/leader/{userIdOfNewLeader}")
    Response<Set<GroupMemberResponse>> changeGroupLeader(@PathVariable Long groupId, @PathVariable Long userIdOfNewLeader);

    /**
     * 같은 성별의 팀 찜하기 취소
     */
    @DeleteMapping("/likes/{toGroupId}")
    Response<Void> deleteSameGenderGroupLike(@PathVariable Long toGroupId);

    /**
     * 다른 성별의 팀 찜하기 취소
     */
    @DeleteMapping("/{fromGroupId}/likes/{toGroupId}")
    Response<Void> deleteOppositeGenderGroupLike(@PathVariable Long fromGroupId, @PathVariable Long toGroupId);

    /**
     * 팀장 - 과팅 요청 취소
     */
    @DeleteMapping("/{fromGroupId}/dates/requests/{toGroupId}")
    Response<Void> deleteGroupDateRequest(@PathVariable Long fromGroupId, @PathVariable Long toGroupId);

    /**
     * 팀장 - 과팅 요청 거절
     */
    @DeleteMapping("/dates/requests/{groupDateRequestId}")
    Response<Void> rejectGroupDateRequest(@PathVariable Long groupDateRequestId);

    /**
     * 같은 성별인 팀에 했던 요청을 취소
     */
    @DeleteMapping("/requests/{groupId}")
    Response<Void> deleteJoinRequest(@PathVariable Long groupId);

    /**
     * 팀 나가기
     */
    @DeleteMapping("/{groupId}/members")
    Response<Void> deleteGroupMember(@PathVariable Long groupId);

    /**
     * 팀장 - 팀 멤버 가입 요청 거절
     */
    @DeleteMapping("/members/requests/{groupMemberRequestId}")
    Response<Void> rejectJoinRequestToMyGroup(@PathVariable Long groupMemberRequestId);
}
