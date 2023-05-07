package com.ting.ting.service;

public interface BlindRequestService {

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
