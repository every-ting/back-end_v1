package com.ting.ting.controller;

import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.*;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.GroupLikeService;
import com.ting.ting.service.GroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;


@RestController
public class GroupControllerImpl extends AbstractController implements GroupController {

    private final GroupService groupService;
    private final GroupLikeService groupLikeService;

    public GroupControllerImpl(GroupService groupService, GroupLikeService groupLikeService) {
        super(ServiceType.GROUP_MEETING);
        this.groupService = groupService;
        this.groupLikeService = groupLikeService;
    }

    @Override
    public Response<Page<GroupWithRequestStatusResponse>> getJoinableSameGenderGroupList(Pageable pageable) {
        Long userId = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupService.findJoinableSameGenderGroupList(userId, pageable));
    }

    @Override
    public Response<Page<GroupWithLikeStatusResponse>> getOppositeGenderGroupList(Long groupId, Pageable pageable) {
        Long userId = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupService.findDateableOppositeGenderGroupList(groupId, userId, pageable));
    }

    @Override
    public Response<Set<MyGroupResponse>> myGroupList() {
        Long userId = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupService.findMyGroupList(userId));
    }

    @Override
    public Response<Void> createSameGenderGroupLike(Long toGroupId) {
        Long fromUserId = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정

        groupLikeService.createSameGenderGroupLike(toGroupId, fromUserId);
        return success();
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
    public Response<Void> deleteGroupDateRequest(Long fromGroupId, Long toGroupId) {
        Long userIdOfLeader = 1L;

        groupService.deleteGroupDateRequest(userIdOfLeader, fromGroupId, toGroupId);
        return success();
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
