package com.ting.ting.controller;

import com.ting.ting.dto.request.SendBlindRequest;
import com.ting.ting.dto.response.BlindDateResponse;
import com.ting.ting.dto.response.BlindUserWithRequestStatusResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.BlindRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class BlindDateControllerImpl extends AbstractController implements BlindDateController {

    private final BlindRequestService blindRequestService;

    public BlindDateControllerImpl(BlindRequestService blindRequestService) {
        super(ServiceType.BLIND);
        this.blindRequestService = blindRequestService;
    }

    @Override
    public Response<Set<BlindUserWithRequestStatusResponse>> blindUsersInfo(Pageable pageable) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindRequestService.blindUsersInfo(userId, pageable));
    }

    @Override
    public Response<Void> sendJoinRequest(SendBlindRequest request) {
        Long fromUserId = 9L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        blindRequestService.createJoinRequest(fromUserId, request.getToUserId());
        return success();
    }

    @Override
    public Response<Void> deleteJoinRequest(long blindRequestId) {
        blindRequestService.deleteRequest(blindRequestId);
        return success();
    }

    @Override
    public Response<List<BlindDateResponse>> confirmOfMyRequest() {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindRequestService.myRequest(userId).stream().collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public Response<List<BlindDateResponse>> confirmOfRequestToMe() {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindRequestService.requestToMe(userId).stream().collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public Response<Void> acceptedRequest(long blindRequestId) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindRequestService.acceptRequest(userId, blindRequestId);
        return success();
    }

    @Override
    public Response<Void> rejectRequest(long blindRequestId) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindRequestService.rejectRequest(userId, blindRequestId);
        return success();
    }
}
