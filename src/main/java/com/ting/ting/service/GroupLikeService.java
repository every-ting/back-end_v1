package com.ting.ting.service;

public interface GroupLikeService {

    /**
     * 같은 성별의 팀 찜하기
     */
    void createSameGenderGroupLike(Long groupId, Long userId);

    /**
     * 같은 성별의 팀 찜하기 취소
     */
    void deleteSameGenderGroupLike(Long groupId, Long userId);
}
