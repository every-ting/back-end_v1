package com.ting.ting.service;

import com.ting.ting.dto.GroupDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface GroupService {

    /**
     * 모든 팀 조회
     */
    Page<GroupDto> findAllGroups(Pageable pageable);

    /**
     * 같은 성별 이면서 내가 속한 팀이 아닌 팀 조회
     */
    Page<GroupDto> findSuggestedGroupList(Pageable pageable);

    /**
     * 내가 속한 팀 조회 - request status : ACCEPTED
     */
    Set<GroupDto> findMyGroupList(Long userId);

    /**
     * 그룹 생성
     */
    GroupDto saveGroup(Long userId, GroupDto dto);

    /**
     * 같은 성별인 팀에 요청
     */
    void saveJoinRequest(long groupId, long userId);

    /**
     * 같은 성별인 팀에 했던 요청을 취소
     */
    void deleteJoinRequest(long groupId, long userId);
}
