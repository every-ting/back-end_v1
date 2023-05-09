package com.ting.ting.controller;

import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.GroupMemberRequestResponse;
import com.ting.ting.dto.response.GroupMemberResponse;
import com.ting.ting.dto.response.GroupResponse;
import com.ting.ting.dto.response.Response;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequestMapping("/groups")
public interface GroupController {

    /**
     * 모든 팀 조회
     */
    @GetMapping
    Response<Page<GroupResponse>> suggestedGroupList(@ParameterObject Pageable pageable);

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
    Response<Void> sendJoinRequest(@PathVariable long groupId);

    /**
     * 같은 성별인 팀에 했던 요청을 취소
     */
    @DeleteMapping("/request/{groupId}")
    Response<Void> deleteJoinRequest(@PathVariable long groupId);

    /**
     * 팀장 넘기기
     */
    @PutMapping("/{groupId}/leader/{newLeaderId}")
    Response<Set<GroupMemberResponse>> changeGroupLeader(@PathVariable Long groupId, @PathVariable Long newLeaderId);

    /**
     * 팀 멤버 가입 요청 조회
     */
    @GetMapping("/{groupId}/members/requests")
    Response<Set<GroupMemberRequestResponse>> getMemberRequestToJoinMyGroup(@PathVariable Long groupId);

    /**
     * 팀 멤버 가입 요청 수락
     */
    @PostMapping("members/requests/{groupMemberRequestId}")
    Response<GroupMemberResponse> acceptJoinRequestToMyGroup(@PathVariable Long groupMemberRequestId);

    /**
     * 팀 멤버 가입 요청 거절
     */
    @DeleteMapping("members/requests/{groupMemberRequestId}")
    Response<Void> rejectJoinRequestToMyGroup(@PathVariable Long groupMemberRequestId);
}
