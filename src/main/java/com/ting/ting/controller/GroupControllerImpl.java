package com.ting.ting.controller;

import com.ting.ting.dto.request.GroupCreateRequest;
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
        return success(groupService.findJoinableSameGenderGroupList(pageable));
    }

    @Override
    public Response<GroupDetailResponse> getGroupDetail(Long groupId) {
        return success(groupService.findGroupDetail(groupId));
    }

    @Override
    public Response<Page<DateableGroupResponse>> getOppositeGenderGroupList(Long groupId, Pageable pageable) {
        return success(groupService.findDateableOppositeGenderGroupList(groupId, pageable));
    }

    @Override
    public Response<Set<MyGroupResponse>> myGroupList() {
        return success(groupService.findMyGroupList());
    }

    @Override
    public Response<Void> createSameGenderGroupLike(Long toGroupId) {
        groupLikeService.createSameGenderGroupLike(toGroupId);
        return success();
    }

    @Override
    public Response<Void> deleteSameGenderGroupLike(Long toGroupId) {
        groupLikeService.deleteSameGenderGroupLike(toGroupId);
        return success();
    }

    @Override
    public Response<Void> createOppositeGenderGroupLike(Long fromGroupId, Long toGroupId) {
        groupLikeService.createOppositeGenderGroupLike(fromGroupId, toGroupId);
        return success();
    }

    @Override
    public Response<Void> deleteOppositeGenderGroupLike(Long fromGroupId, Long toGroupId) {
        groupLikeService.deleteOppositeGenderGroupLike(fromGroupId, toGroupId);
        return success();
    }

    @Override
    public Response<Page<DateableGroupResponse>> getGroupLikeToDateList(Long groupId, Pageable pageable) {
        return success(groupLikeService.findGroupLikeToDateList(groupId, pageable));
    }

    @Override
    public Response<Page<JoinableGroupResponse>> getGroupLikeToJoinList(Pageable pageable) {
        return success(groupLikeService.findGroupLikeToJoinList(pageable));
    }

    @Override
    public Response<GroupResponse> createGroup(GroupCreateRequest request) {
        return success(groupService.saveGroup(request));
    }

    @Override
    public Response<Page<DateableGroupResponse>> getGroupDateRequest(Long groupId, Pageable pageable) {
        return success(groupDateService.findGroupDateRequests(groupId, pageable));
    }

    @Override
    public Response<GroupDateRequestResponse> saveGroupDateRequest(Long fromGroupId, Long toGroupId) {
        return success(groupDateService.saveGroupDateRequest(fromGroupId, toGroupId));
    }

    @Override
    public Response<Void> deleteGroupDateRequest(Long fromGroupId, Long toGroupId) {
        groupDateService.deleteGroupDateRequest(fromGroupId, toGroupId);
        return success();
    }

    @Override
    public Response<GroupDateResponse> acceptGroupDateRequest(Long groupDateRequestId) {
        return success(groupDateService.acceptGroupDateRequest(groupDateRequestId));
    }

    @Override
    public Response<Void> rejectGroupDateRequest(Long groupDateRequestId) {
        groupDateService.rejectGroupDateRequest(groupDateRequestId);
        return success();
    }

    @Override
    public Response<Set<GroupMemberResponse>> getGroupMemberList(Long groupId) {
        return success(groupMemberService.findGroupMemberList(groupId));
    }

    @Override
    public Response<Page<JoinableGroupResponse>> getUserJoinRequestList(Pageable pageable) {
        return success(groupMemberService.findUserJoinRequestList(pageable));
    }

    @Override
    public Response<Void> sendJoinRequest(Long groupId) {
        groupMemberService.saveJoinRequest(groupId);
        return success();
    }

    @Override
    public Response<Void> deleteJoinRequest(Long groupId) {
        groupMemberService.deleteJoinRequest(groupId);
        return success();
    }

    @Override
    public Response<Void> deleteGroupMember(Long groupId) {
        groupMemberService.deleteGroupMember(groupId);
        return success();
    }

    @Override
    public Response<Set<GroupMemberResponse>> changeGroupLeader(Long groupId, Long userIdOfNewLeader) {
        return success(groupMemberService.changeGroupLeader(groupId, userIdOfNewLeader));
    }

    @Override
    public Response<Set<GroupMemberRequestResponse>> getMemberRequestToJoinMyGroup(Long groupId) {
        return success(groupMemberService.findMemberJoinRequest(groupId));
    }

    @Override
    public Response<GroupMemberResponse> acceptJoinRequestToMyGroup(Long groupMemberRequestId) {
        return success(groupMemberService.acceptMemberJoinRequest(groupMemberRequestId));
    }

    @Override
    public Response<Void> rejectJoinRequestToMyGroup(Long groupMemberRequestId) {
        groupMemberService.rejectMemberJoinRequest(groupMemberRequestId);
        return success();
    }
}
