package com.ting.ting.controller;

import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.dto.request.BlindRequest;
import com.ting.ting.dto.response.BlindRequestWithFromAndToResponse;
import com.ting.ting.dto.response.BlindUserWithRequestStatusResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.BlindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class BlindDateControllerImpl extends AbstractController implements BlindDateController {

    private final BlindService blindService;

    public BlindDateControllerImpl(BlindService blindService) {
        super(ServiceType.BLIND);
        this.blindService = blindService;
    }

    @Override
    public Response<Page<BlindUserWithRequestStatusResponse>> getBlindUsersInfo(Pageable pageable) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindService.blindUsersInfo(userId, pageable));
    }

    @Override
    public Response<Void> sendJoinRequest(BlindRequest request) {
        Long fromUserId = 9L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        blindService.createJoinRequest(fromUserId, request.getToUserId());
        return success();
    }

    @Override
    public Response<Void> deleteJoinRequest(BlindRequest blindRequest) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindService.deleteRequestByFromUserIdAndToUserId(userId, blindRequest.getToUserId());
        return success();
    }

    @Override
    public Response<Void> deleteJoinRequest(long blindRequestId) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindService.deleteRequestByBlindRequestId(userId, blindRequestId);
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
    public Response<Void> sendJoinLiked(BlindRequest request) {
        Long fromUserId = 9L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        blindService.createJoinLiked(fromUserId, request.getToUserId());
        return success();
    }

    @Override
    public Response<Void> deleteJoinLiked(BlindRequest blindRequest) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindService.deleteLikedByFromUserIdAndToUserId(userId, blindRequest.getToUserId());
        return success();
    }

    @Override
    public Response<Void> deleteJoinLiked(long blindRequestId) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindService.deleteLikedByBlindRequestId(userId, blindRequestId);
        return success();
    }

    @Override
    public Response<BlindRequestWithFromAndToResponse> getBlindLiked() {
        return null;
    }
}
