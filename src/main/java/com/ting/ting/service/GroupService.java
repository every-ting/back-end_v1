package com.ting.ting.service;

import com.ting.ting.dto.request.GroupCreateRequest;
import com.ting.ting.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface GroupService {

    /**
     * 모든 팀 조회
     */
    Page<GroupResponse> findAllGroups(Pageable pageable);

    /**
     * 팀 상세 조회
     */
    GroupDetailResponse findGroupDetail(Long groupId);

    /**
     * 같은 성별 이면서 내가 속한 팀이 아닌 팀 조회
     */
    Page<JoinableGroupResponse> findJoinableSameGenderGroupList(Pageable pageable);

    /**
     * 다른 성별의 팀 조회
     */
    Page<DateableGroupResponse> findDateableOppositeGenderGroupList(Long groupId, Pageable pageable);

    /**
     * 내가 속한 팀 조회
     */
    Set<MyGroupResponse> findMyGroupList();

    /**
     * 그룹 생성
     */
    GroupResponse saveGroup(GroupCreateRequest request);
}
