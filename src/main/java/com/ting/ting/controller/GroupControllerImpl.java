package com.ting.ting.controller;

import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.GroupResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
public class GroupControllerImpl extends AbstractController implements GroupController {

    private final GroupService groupService;

    public GroupControllerImpl(GroupService groupService) {
        super(ServiceType.GROUP_MEETING);
        this.groupService = groupService;
    }

    @Override
    public Response<Page<GroupResponse>> suggestedGroupList(@ParameterObject Pageable pageable) {
        return success(groupService.findAllGroups(pageable).map(GroupResponse::from));
    }

    @Override
    public Response<List<GroupResponse>> myGroupList() {
        // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(groupService.findMyGroupList(1L).stream().map(GroupResponse::from).collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public Response<GroupResponse> createGroup(@RequestBody GroupRequest request) {
        Long userId = 9L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(GroupResponse.from(groupService.saveGroup(userId, request.toDto())));
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
