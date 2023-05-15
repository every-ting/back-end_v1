package com.ting.ting.controller;

import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.*;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.GroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;


@RestController
public class GroupControllerImpl extends AbstractController implements GroupController {

    private final GroupService groupService;

    public GroupControllerImpl(GroupService groupService) {
        super(ServiceType.GROUP_MEETING);
        this.groupService = groupService;
    }

    @Override
    public Response<Page<GroupWithRequestStatusResponse>> getSuggestedSameGenderGroupList(Pageable pageable) {
        Long userId = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(groupService.findSuggestedSameGenderGroupList(userId, pageable));
    }

    @Override
    public Response<Set<GroupResponse>> myGroupList() {
        // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(groupService.findMyGroupList(1L));
    }

    @Override
    public Response<Set<GroupMemberResponse>> getGroupMemberList(Long groupId) {
        return success(groupService.findGroupMemberList(groupId));
    }

    @Override
    public Response<GroupResponse> createGroup(GroupRequest request) {
        Long userId = 9L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(groupService.saveGroup(userId, request));
    }

    @Override
    public Response<Void> sendJoinRequest(Long groupId) {
        Long userId = groupId + 1;  // userId를 임의로 설정 TODO: user 구현 후 수정

        groupService.saveJoinRequest(groupId, userId);
        return success();
    }

    @Override
    public Response<Void> deleteJoinRequest(Long groupId) {
        Long userId = groupId + 1;  // userId를 임의로 설정 TODO: user 구현 후 수정

        groupService.deleteJoinRequest(groupId, userId);
        return success();
    }
    
    @Override
    public Response<Void> deleteGroupMember(Long groupId) {
        Long userId = 1L;

        groupService.deleteGroupMember(groupId, userId);
        return success();
    }

    @Override
    public Response<Set<GroupMemberResponse>> changeGroupLeader(Long groupId, Long userIdOfNewLeader) {
        Long userIdOfLeader = 1L;  // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupService.changeGroupLeader(groupId, userIdOfLeader, userIdOfNewLeader));
    }

    @Override
    public Response<Set<GroupMemberRequestResponse>> getMemberRequestToJoinMyGroup(Long groupId) {
        Long userIdOfLeader = 1L;  // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupService.findMemberJoinRequest(groupId, userIdOfLeader));
    }

    @Override
    public Response<GroupMemberResponse> acceptJoinRequestToMyGroup(Long groupMemberRequestId) {
        Long userIdOfLeader = 1L;  // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupService.acceptMemberJoinRequest(userIdOfLeader, groupMemberRequestId));
    }

    @Override
    public Response<Void> rejectJoinRequestToMyGroup(Long groupMemberRequestId) {
        Long userIdOfLeader = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정

        groupService.rejectMemberJoinRequest(userIdOfLeader, groupMemberRequestId);
        return success();
    }

    @Override
    public Response<GroupDateRequestWithFromAndToResponse> getGroupDateRequest(Long groupId) {
        Long userIdOfLeader = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupService.findAllGroupDateRequest(groupId, userIdOfLeader));
    }

    @Override
    public Response<GroupDateRequestResponse> saveGroupDateRequest(Long fromGroupId, Long toGroupId) {
        Long userIdOfLeader = 1L;

        return success(groupService.saveGroupDateRequest(userIdOfLeader, fromGroupId, toGroupId));
    }

    @Override
    public Response<GroupDateResponse> acceptGroupDateRequest(Long groupDateRequestId) {
        Long userIdOfLeader = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupService.acceptGroupDateRequest(userIdOfLeader, groupDateRequestId));
    }

    @Override
    public Response<Void> rejectGroupDateRequest(Long groupDateRequestId) {
        Long userIdOfLeader = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정

        groupService.rejectGroupDateRequest(userIdOfLeader, groupDateRequestId);
        return success();
    }
}
