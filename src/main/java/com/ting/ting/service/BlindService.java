package com.ting.ting.service;

import com.ting.ting.dto.response.BlindRequestWithFromAndToResponse;
import com.ting.ting.dto.response.BlindUserWithRequestStatusAndLikeStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlindService {

    /**
     * 소개팅 상대편 조회(자신의 성별에 따라 조회 결과가 다름)
     */
    Page<BlindUserWithRequestStatusAndLikeStatusResponse> blindUsersInfo(Long userId, Pageable pageable);

    // Todo :: 요청하기

    /**
     * 소개팅 상대에게 요청
     */
    void createJoinRequest(long fromUserId, long toUserId);

    /**
     * 소개팅 상대에게 한 요청 취소
     */
    void deleteRequestById(long userId, long toUserId);

    /**
     * 소개팅 요청 조회(받은 요청, 한 요청 모두)
     */
    BlindRequestWithFromAndToResponse getBlindRequest(long userId);

    /**
     * 자신에게 온 요청 수락
     */
    void acceptRequest(long userId, long blindRequestId);

    /**
     * 자신에게 온 요청 거절
     */
    void rejectRequest(long userId, long blindRequestId);
}
