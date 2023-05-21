package com.ting.ting.controller;

import com.ting.ting.dto.request.BlindRequest;
import com.ting.ting.dto.response.BlindRequestWithFromAndToResponse;
import com.ting.ting.dto.response.BlindUserWithRequestStatusResponse;
import com.ting.ting.dto.response.Response;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/blind")
public interface BlindDateController {

    /**
     * 소개팅 상대편 조회
     */
    @GetMapping("/users")
    Response<Page<BlindUserWithRequestStatusResponse>> getBlindUsersInfo(@ParameterObject Pageable pageable);

    //Todo :: 소개팅 요청 로직

    /**
     * 소개팅 상대에게 요청
     */
    @PostMapping("/request")
    Response<Void> sendJoinRequest(@RequestBody BlindRequest request);

    /**
     * 소개팅 상대에게 한 요청 취소 -> 상대편 조회 페이지에서
     */
    @PostMapping("/request/cancel")
    Response<Void> deleteJoinRequest(@RequestBody BlindRequest blindRequest);

    /**
     * 소개팅 상대에게 한 요청 취소 -> 요청 페이지에서
     */
    @DeleteMapping("/request/{blindRequestId}")
    Response<Void> deleteJoinRequest(@PathVariable long blindRequestId);

    /**
     * 소개팅 요청 조회(받은 요청, 한 요청 모두)
     */
    @GetMapping("/requests")
    Response<BlindRequestWithFromAndToResponse> getBlindRequest();

    /**
     * 자신에게 온 요청 수락
     */
    @PutMapping("/request/accept/{blindRequestId}")
    Response<Void> acceptRequest(@PathVariable long blindRequestId);

    /**
     * 자신에게 온 요청 거절
     */
    @PutMapping("/request/reject/{blindRequestId}")
    Response<Void> rejectRequest(@PathVariable long blindRequestId);

    //Todo :: 찜 로직

    /**
     * 소개팅 상대방 찜하기
     */
    @PostMapping("/like")
    Response<Void> sendJoinLiked(@RequestBody BlindRequest request);

    /**
     * 소개팅 상대에게 한 찜하기 취소 -> 상대편 조회 페이지에서
     */
    @PostMapping("/like/cancel")
    Response<Void> deleteJoinLiked(@RequestBody BlindRequest blindRequest);

    /**
     * 소개팅 상대에게 한 찜하기 취소 -> 요청 페이지에서
     */
    @DeleteMapping("/like/{blindRequestId}")
    Response<Void> deleteJoinLiked(@PathVariable long blindRequestId);

    /**
     * 소개팅 찜하기 조회(받은 요청, 한 요청 모두)
     */
    @GetMapping("/likes")
    Response<BlindRequestWithFromAndToResponse> getBlindLiked();
}
