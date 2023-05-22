package com.ting.ting.controller;

import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.dto.response.BlindLikeResponse;
import com.ting.ting.dto.response.BlindRequestWithFromAndToResponse;
import com.ting.ting.dto.response.BlindUserWithRequestStatusAndLikeStatusResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.BlindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Slf4j
@RestController
public class BlindDateControllerImpl extends AbstractController implements BlindDateController {

    private final BlindService blindService;

    public BlindDateControllerImpl(BlindService blindService) {
        super(ServiceType.BLIND);
        this.blindService = blindService;
    }

    @Override
    public Response<Page<BlindUserWithRequestStatusAndLikeStatusResponse>> getBlindUsersInfo(Pageable pageable) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindService.blindUsersInfo(userId, pageable));
    }

    @Override
    public Response<Void> sendJoinRequest(long toUserId) {
        Long fromUserId = 9L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        blindService.createJoinRequest(fromUserId, toUserId);
        return success();
    }

    @Override
    public Response<Void> deleteJoinRequest(long toUserId) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindService.deleteRequestById(userId, toUserId);
        return success();
    }

    @Override
    public Response<BlindRequestWithFromAndToResponse> getBlindRequest() {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindService.getBlindRequest(userId));
    }

    @Override
    public Response<Void> acceptRequest(long blindRequestId) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindService.handleRequest(userId, blindRequestId, RequestStatus.ACCEPTED);
        return success();
    }

    @Override
    public Response<Void> rejectRequest(long blindRequestId) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindService.handleRequest(userId, blindRequestId, RequestStatus.REJECTED);
        return success();
    }

    @Override
    public Response<Void> sendJoinLiked(long toUserId) {
        Long fromUserId = 9L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        blindService.createJoinLiked(fromUserId, toUserId);
        return success();
    }

    @Override
    public Response<Void> deleteJoinLiked(long toUserId) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindService.deleteLikedByFromUserIdAndToUserId(userId, toUserId);
        return success();
    }

    @Override
    public Response<Set<BlindLikeResponse>> getBlindLiked() {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindService.getBlindLike(userId));
    }
}
