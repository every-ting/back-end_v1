package com.ting.ting.service;

import com.ting.ting.dto.response.BlindUsersInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlindRequestService {

    /**
     * 소개팅 상대편 조회(자신의 성별에 따라 조회 결과가 다름)
     */
    Page<BlindUsersInfoResponse> blindUsersInfo(Long userId, Pageable pageable);

    /**
     * 소개팅 상대에게 요청
     */
    void createJoinRequest(long fromUserId, long toUserId);

    /**
     * 소개팅 상대에게 한 요청 취소
     */
    void deleteRequest(long blindRequestId);

    /**
     * 자신에게 온 요청 수락 -> 추가 구현 필요
     */
    void acceptRequest(long blindRequestId);

    /**
     * 자신에게 온 요청 거절 -> 추가 구현 필요
     */
    void rejectRequest(long blindRequestId);
}
