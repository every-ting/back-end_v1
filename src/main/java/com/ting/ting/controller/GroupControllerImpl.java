package com.ting.ting.controller;

import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.*;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.GroupDateService;
import com.ting.ting.service.GroupLikeService;
import com.ting.ting.service.GroupMemberService;
import com.ting.ting.service.GroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;


@RestController
public class GroupControllerImpl extends AbstractController implements GroupController {

    private final GroupService groupService;
    private final GroupMemberService groupMemberService;
    private final GroupLikeService groupLikeService;
    private final GroupDateService groupDateService;

    public GroupControllerImpl(GroupService groupService, GroupMemberService groupMemberService, GroupLikeService groupLikeService, GroupDateService groupDateService) {
        super(ServiceType.GROUP_MEETING);
        this.groupService = groupService;
        this.groupMemberService = groupMemberService;
        this.groupLikeService = groupLikeService;
        this.groupDateService = groupDateService;
    }

    @Override
    public Response<Page<JoinableGroupResponse>> getJoinableSameGenderGroupList(Pageable pageable) {
        Long userId = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupService.findJoinableSameGenderGroupList(userId, pageable));
    }

    @Override
    public Response<GroupDetailResponse> getGroupDetail(Long groupId) {
        Long userId = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupService.findGroupDetail(groupId, userId));
    }

    @Override
    public Response<Page<DateableGroupResponse>> getOppositeGenderGroupList(Long groupId, Pageable pageable) {
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
    public Response<Void> deleteSameGenderGroupLike(Long toGroupId) {
        Long fromUserId = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정

        groupLikeService.deleteSameGenderGroupLike(toGroupId, fromUserId);
        return success();
    }

    @Override
    public Response<Void> createOppositeGenderGroupLike(Long fromGroupId, Long toGroupId) {
        Long userId = 1L;

        groupLikeService.createOppositeGenderGroupLike(fromGroupId, toGroupId, userId);
        return success();
    }

    @Override
    public Response<Void> deleteOppositeGenderGroupLike(Long fromGroupId, Long toGroupId) {
        Long userId = 1L;

        groupLikeService.deleteOppositeGenderGroupLike(fromGroupId, toGroupId, userId);
        return success();
    }

    @Override
    public Response<Page<DateableGroupResponse>> getGroupLikeToDateList(Long groupId, Pageable pageable) {
        Long userId = 1L;

        return success(groupLikeService.findGroupLikeToDateList(groupId, userId, pageable));
    }

    @Override
    public Response<Page<JoinableGroupResponse>> getGroupLikeToJoinList(Pageable pageable) {
        Long userId = 1L;

        return success(groupLikeService.findGroupLikeToJoinList(userId, pageable));
    }

    @Override
    public Response<GroupResponse> createGroup(GroupRequest request) {
        Long userId = 9L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(groupService.saveGroup(userId, request));
    }

    @Override
    public Response<Page<DateableGroupResponse>> getGroupDateRequest(Long groupId, Pageable pageable) {
        Long userId = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupDateService.findGroupDateRequests(groupId, userId, pageable));
    }

    @Override
    public Response<GroupDateRequestResponse> saveGroupDateRequest(Long fromGroupId, Long toGroupId) {
        Long userIdOfLeader = 1L;

        return success(groupDateService.saveGroupDateRequest(userIdOfLeader, fromGroupId, toGroupId));
    }

    @Override
    public Response<Void> deleteGroupDateRequest(Long fromGroupId, Long toGroupId) {
        Long userIdOfLeader = 1L;

        groupDateService.deleteGroupDateRequest(userIdOfLeader, fromGroupId, toGroupId);
        return success();
    }

    @Override
    public Response<GroupDateResponse> acceptGroupDateRequest(Long groupDateRequestId) {
        Long userIdOfLeader = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupDateService.acceptGroupDateRequest(userIdOfLeader, groupDateRequestId));
    }

    @Override
    public Response<Void> rejectGroupDateRequest(Long groupDateRequestId) {
        Long userIdOfLeader = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정

        groupDateService.rejectGroupDateRequest(userIdOfLeader, groupDateRequestId);
        return success();
    }

    @Override
    public Response<Set<GroupMemberResponse>> getGroupMemberList(Long groupId) {
        return success(groupMemberService.findGroupMemberList(groupId));
    }

    @Override
    public Response<Page<JoinableGroupResponse>> getUserJoinRequestList(Pageable pageable) {
        Long userId = 1L;  // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupMemberService.findUserJoinRequestList(userId, pageable));
    }

    @Override
    public Response<Void> sendJoinRequest(Long groupId) {
        Long userId = groupId + 1;  // userId를 임의로 설정 TODO: user 구현 후 수정

        groupMemberService.saveJoinRequest(groupId, userId);
        return success();
    }

    @Override
    public Response<Void> deleteJoinRequest(Long groupId) {
        Long userId = groupId + 1;  // userId를 임의로 설정 TODO: user 구현 후 수정

        groupMemberService.deleteJoinRequest(groupId, userId);
        return success();
    }

    @Override
    public Response<Void> deleteGroupMember(Long groupId) {
        Long userId = 1L;

        groupMemberService.deleteGroupMember(groupId, userId);
        return success();
    }

    @Override
    public Response<Set<GroupMemberResponse>> changeGroupLeader(Long groupId, Long userIdOfNewLeader) {
        Long userIdOfLeader = 1L;  // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupMemberService.changeGroupLeader(groupId, userIdOfLeader, userIdOfNewLeader));
    }

    @Override
    public Response<Set<GroupMemberRequestResponse>> getMemberRequestToJoinMyGroup(Long groupId) {
        Long userIdOfLeader = 1L;  // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupMemberService.findMemberJoinRequest(groupId, userIdOfLeader));
    }

    @Override
    public Response<GroupMemberResponse> acceptJoinRequestToMyGroup(Long groupMemberRequestId) {
        Long userIdOfLeader = 1L;  // userId를 임의로 설정 TODO: user 구현 후 수정

        return success(groupMemberService.acceptMemberJoinRequest(userIdOfLeader, groupMemberRequestId));
    }

    @Override
    public Response<Void> rejectJoinRequestToMyGroup(Long groupMemberRequestId) {
        Long userIdOfLeader = 1L; // userId를 임의로 설정 TODO: user 구현 후 수정

        groupMemberService.rejectMemberJoinRequest(userIdOfLeader, groupMemberRequestId);
        return success();
    }
}
