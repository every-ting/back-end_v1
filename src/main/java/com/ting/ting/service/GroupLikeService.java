package com.ting.ting.service;

import com.ting.ting.dto.response.DateableGroupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupLikeService {

    /**
     * 팀 기준 - 찜한 목록 조회
     */
    Page<DateableGroupResponse> findGroupLikeToDateList(Long groupId, Long userId, Pageable pageable);

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
