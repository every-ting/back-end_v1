package com.ting.ting.controller;

import com.ting.ting.dto.response.GroupResponse;
import com.ting.ting.dto.response.Response;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin")
public interface AdminController {

    /**
     * 모든 팀 조회
     */
    @GetMapping("/groups")
    Response<Page<GroupResponse>> getGroupList(@ParameterObject Pageable pageable);

}
