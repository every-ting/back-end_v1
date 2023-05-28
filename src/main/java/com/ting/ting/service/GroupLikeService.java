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

    /**
     * 다른 성별의 팀 찜하기
     */
    void createOppositeGenderGroupLike(Long fromGroupId, Long toGroupId, Long userIdOfMember);

    /**
     * 다른 성별의 팀 찜하기 취소
     */
    void deleteOppositeGenderGroupLike(Long fromGroupId, Long toGroupId, Long userIdOfMember);
}
