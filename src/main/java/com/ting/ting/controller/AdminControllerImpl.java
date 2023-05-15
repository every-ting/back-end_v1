package com.ting.ting.controller;

import com.ting.ting.dto.response.GroupResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.GroupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminControllerImpl extends AbstractController implements AdminController{

    private final GroupService groupService;

    public AdminControllerImpl(GroupService groupService) {
        super(ServiceType.ADMIN);
        this.groupService = groupService;
    }

    @Override
    public Response<Page<GroupResponse>> getGroupList(Pageable pageable) {
        return success(groupService.findAllGroups(pageable));
    }
}
