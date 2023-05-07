package com.ting.ting.controller;

import com.ting.ting.dto.request.SendBlindRequest;
import com.ting.ting.dto.response.BlindUsersInfoResponse;
import com.ting.ting.dto.response.Response;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/blind")
public interface BlindDateController {

    /**
     * 소개팅 상대편 조회(자신의 성별에 따라 조회 결과가 다름)
     */
    @GetMapping("/users")
    Response<Page<BlindUsersInfoResponse>> blindUsersInfo(@ParameterObject Pageable pageable);

    /**
     * 소개팅 상대에게 요청
     */
    @PostMapping("/request")
    Response<Void> sendJoinRequest(@RequestBody SendBlindRequest request);

    /**
     * 소개팅 상대에게 한 요청 취소
     */
    @DeleteMapping("/request/{blindRequestId}")
    Response<Void> deleteJoinRequest(@PathVariable long blindRequestId);

    /**
     * 자신에게 온 요청 수락 -> 추가 구현 필요
     */
    @PutMapping("/request/accept/{blindRequestId}")
    Response<Void> acceptedRequest(@PathVariable long blindRequestId);

    /**
     * 자신에게 온 요청 거절 -> 추가 구현 필요
     */
    @PutMapping("/request/reject/{blindRequestId}")
    Response<Void> rejectRequest(@PathVariable long blindRequestId);
}
