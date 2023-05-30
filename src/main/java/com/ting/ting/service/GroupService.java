package com.ting.ting.service;

import com.ting.ting.dto.request.GroupRequest;
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
     * 같은 성별 이면서 내가 속한 팀이 아닌 팀 조회
     */
    Page<JoinableGroupResponse> findJoinableSameGenderGroupList(Long userId, Pageable pageable);

    /**
     * 다른 성별의 팀 조회
     */
    Page<DateableGroupResponse> findDateableOppositeGenderGroupList(Long groupId, Long userId, Pageable pageable);

    /**
     * 내가 속한 팀 조회
     */
    Set<MyGroupResponse> findMyGroupList(Long userId);

    /**
     * 그룹 생성
     */
    GroupResponse saveGroup(Long userId, GroupRequest request);

    /**
     * 팀장 - 팀이 한 과팅 요청과, 받은 과팅 요청 모두 조회
     */
    GroupDateRequestWithFromAndToResponse findAllGroupDateRequest(long groupId, long userIdOfLeader);

    /**
     * 팀장 - 다른 팀에 과팅 요청
     */
    GroupDateRequestResponse saveGroupDateRequest(long userIdOfLeader, long fromGroupId, long toGroupId);

    /**
     * 팀장 - 다른 팀에 했던 과팅 요청을 취소
     */
    void deleteGroupDateRequest(long userIdOfLeader, long fromGroupId, long toGroupId);

    /**
     * 팀장 - 팀에 온 과팅 요청 수락
     */
    GroupDateResponse acceptGroupDateRequest(long userIdOfLeader, long groupDateRequestId);

    /**
     * 팀장 - 팀에 온 과팅 요청 거절
     */
    void rejectGroupDateRequest(long userIdOfLeader, long groupDateRequestId);
}
