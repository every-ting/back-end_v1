package com.ting.ting.controller;

import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.GroupMemberResponse;
import com.ting.ting.dto.response.GroupResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.GroupService;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
    public Response<Page<GroupResponse>> suggestedGroupList(@ParameterObject Pageable pageable) {
        return success(groupService.findAllGroups(pageable));
    }

    @Override
    public Response<Set<GroupResponse>> myGroupList() {
        // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(groupService.findMyGroupList(1L));
    }

    @Override
    public Response<Set<GroupMemberResponse>> getGroupMemberList(@PathVariable Long groupId) {
        return success(groupService.findGroupMemberList(groupId));
    }

    @Override
    public Response<Set<GroupMemberResponse>> changeGroupLeader(@PathVariable Long groupId, @PathVariable Long memberId) {
        Long leaderId = 1L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(groupService.changeGroupLeader(groupId, leaderId, memberId));
    }

    @Override
    public Response<GroupResponse> createGroup(@RequestBody GroupRequest request) {
        Long userId = 9L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(groupService.saveGroup(userId, request));
    }

    @Override
    public Response<Void> sendJoinRequest(@PathVariable long groupId) {
        Long userId = groupId + 1;  // userId를 임의로 설정 TODO: user 구현 후 수정

        groupService.saveJoinRequest(groupId, userId);
        return success();
    }

    @Override
    public Response<Void> deleteJoinRequest(@PathVariable long groupId) {
        Long userId = groupId + 1;  // userId를 임의로 설정 TODO: user 구현 후 수정

        groupService.deleteJoinRequest(groupId, userId);
        return success();
    }
}
