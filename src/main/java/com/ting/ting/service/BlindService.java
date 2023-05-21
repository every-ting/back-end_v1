package com.ting.ting.service;

import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.dto.response.BlindRequestWithFromAndToResponse;
import com.ting.ting.dto.response.BlindUserWithRequestStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlindService {

    /**
     * 소개팅 상대편 조회(자신의 성별에 따라 조회 결과가 다름)
     */
    Page<BlindUserWithRequestStatusResponse> blindUsersInfo(Long userId, Pageable pageable);

    //Todo :: 요청하기

    /**
     * 소개팅 상대에게 요청
     */
    void createJoinRequest(long fromUserId, long toUserId);

    /**
     * 소개팅 상대에게 한 요청 취소 -> 조회 페이지에서
     */
    void deleteRequestByFromUserIdAndToUserId(long userId, long toUserId);

    /**
     * 소개팅 상대에게 한 요청 취소 -> 요청 페이지에서
     */
    void deleteRequestByBlindRequestId(long userId, long blindRequestId);

    /**
     * 소개팅 요청 조회(받은 요청, 한 요청 모두)
     */
    BlindRequestWithFromAndToResponse getBlindRequest(long userId);

    /**
     * 자신에게 온 요청 수락 & 거절
     */
    void handleRequest(long userId, long blindRequestId, RequestStatus requestStatus);

    // Todo :: 찜하기

    /**
     * 소개팅 상대방 찜하기
     */
    void createJoinLiked(long fromUserId, long toUserId);

    /**
     * 소개팅 상대에게 한 찜하기 취소 -> 조회 페이지에서
     */
    void deleteLikedByFromUserIdAndToUserId(long userId, long toUserId);

    /**
     * 소개팅 상대에게 한 찜하기 취소 -> 요청 페이지에서
     */
    void deleteLikedByBlindRequestId(long userId, long blindLikeId);

    /**
     * 소개팅 찜 조회(받은 요청, 한 요청 모두)
     */
    BlindRequestWithFromAndToResponse getBlindLiked(long userId);
}
