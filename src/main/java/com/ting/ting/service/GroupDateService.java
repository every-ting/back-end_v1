package com.ting.ting.service;

import com.ting.ting.dto.response.DateableGroupResponse;
import com.ting.ting.dto.response.GroupDateRequestResponse;
import com.ting.ting.dto.response.GroupDateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupDateService {

    /**
     * 팀이 받은 과팅 요청 조회
     */
    Page<DateableGroupResponse> findGroupDateRequests(long groupId, Pageable pageable);

    /**
     * 팀장 - 다른 팀에 과팅 요청
     */
    GroupDateRequestResponse saveGroupDateRequest(long fromGroupId, long toGroupId);

    /**
     * 팀장 - 다른 팀에 했던 과팅 요청을 취소
     */
    void deleteGroupDateRequest(long fromGroupId, long toGroupId);

    /**
     * 팀장 - 팀에 온 과팅 요청 수락
     */
    GroupDateResponse acceptGroupDateRequest(long groupDateRequestId);

    /**
     * 팀장 - 팀에 온 과팅 요청 거절
     */
    void rejectGroupDateRequest(long groupDateRequestId);
}
