package com.ting.ting.service;

import com.ting.ting.dto.response.DateableGroupResponse;
import com.ting.ting.dto.response.JoinableGroupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupLikeService {

    /**
     * 팀 기준 - 찜한 목록 조회
     */
    Page<DateableGroupResponse> findGroupLikeToDateList(Long groupId, Pageable pageable);

    /**
     * 유저 기준 - 찜한 같은 성별의 팀 조회
     */
    Page<JoinableGroupResponse> findGroupLikeToJoinList(Pageable pageable);

    /**
     * 같은 성별의 팀 찜하기
     */
    void createSameGenderGroupLike(Long groupId);

    /**
     * 같은 성별의 팀 찜하기 취소
     */
    void deleteSameGenderGroupLike(Long groupId);

    /**
     * 다른 성별의 팀 찜하기
     */
    void createOppositeGenderGroupLike(Long fromGroupId, Long toGroupId);

    /**
     * 다른 성별의 팀 찜하기 취소
     */
    void deleteOppositeGenderGroupLike(Long fromGroupId, Long toGroupId);
}
