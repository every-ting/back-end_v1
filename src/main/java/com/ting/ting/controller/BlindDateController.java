package com.ting.ting.controller;

import com.ting.ting.dto.response.BlindLikeResponse;
import com.ting.ting.dto.response.BlindRequestWithFromAndToResponse;
import com.ting.ting.dto.response.BlindUserWithRequestStatusAndLikeStatusResponse;
import com.ting.ting.dto.response.Response;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequestMapping("/blind")
public interface BlindDateController {

    /**
     * 소개팅 상대편 조회
     */
    @GetMapping
    Response<Page<BlindUserWithRequestStatusAndLikeStatusResponse>> getBlindUsersInfo(@ParameterObject Pageable pageable);

    //Todo :: 소개팅 요청 로직

    /**
     * 소개팅 상대에게 요청
     */
    @PostMapping("/request/{toUserId}")
    Response<Void> sendJoinRequest(@PathVariable long toUserId);

    /**
     * 소개팅 상대에게 한 요청 취소
     */
    @DeleteMapping("/request/{toUserId}")
    Response<Void> deleteJoinRequest(@PathVariable long toUserId);

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
    @PostMapping("/like/{toUserId}")
    Response<Void> sendJoinLiked(@PathVariable long toUserId);

    /**
     * 소개팅 상대에게 한 찜하기 취소
     */
    @DeleteMapping("/like/{toUserId}")
    Response<Void> deleteJoinLiked(@PathVariable long toUserId);

    /**
     * 소개팅 찜하기 조회(받은 요청, 한 요청 모두)
     */
    @GetMapping("/likes")
    Response<Set<BlindLikeResponse>> getBlindLiked();
}
