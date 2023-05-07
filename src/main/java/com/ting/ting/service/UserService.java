package com.ting.ting.service;

import com.ting.ting.dto.response.BlindUsersInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    /**
     * 소개팅 상대편 조회(자신의 성별에 따라 조회 결과가 다름)
     */
    Page<BlindUsersInfoResponse> usersInfo(Long userId, Pageable pageable);
}
