package com.ting.ting.service;

import com.ting.ting.dto.response.BlindLikeResponse;

import java.util.Set;

public interface BlindLikeService {

    // Todo :: 찜하기

    /**
     * 소개팅 상대방 찜하기
     */
    void createJoinLiked(long fromUserId, long toUserId);

    /**
     * 소개팅 상대에게 한 찜하기 취소
     */
    void deleteLikedByFromUserIdAndToUserId(long userId, long toUserId);

    /**
     * 소개팅 찜 조회(받은 요청, 한 요청 모두)
     */
    Set<BlindLikeResponse> getBlindLike(long userId);
}
