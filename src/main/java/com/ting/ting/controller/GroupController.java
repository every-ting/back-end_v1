package com.ting.ting.controller;

import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.GroupResponse;
import com.ting.ting.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public Page<GroupResponse> list(Pageable pageable) {
        return groupService.list(pageable).map(GroupResponse::from);
    }

    @GetMapping("/my")
    public List<GroupResponse> mylist() {
        return groupService.myList(1L).stream().map(GroupResponse::from).collect(Collectors.toUnmodifiableList());
    }

    @PostMapping
    public String create(@RequestBody GroupRequest request) {
        groupService.saveGroup(request.toDto());
        return "success";
    }

    @PostMapping("/request/{groupId}")
    public String request(@PathVariable long groupId) {
        groupService.createJoinRequest(groupId);
        return "success";
    }

    @DeleteMapping("/request/{groupId}")
    public String deleteRequest(@PathVariable long groupId) {
        groupService.deleteJoinRequest(groupId);
        return "success";
    }
}
