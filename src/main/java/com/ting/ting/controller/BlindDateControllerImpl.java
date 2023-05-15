package com.ting.ting.controller;

import com.ting.ting.dto.request.SendBlindRequest;
import com.ting.ting.dto.response.BlindDateResponse;
import com.ting.ting.dto.response.BlindUserWithRequestStatusResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.BlindDateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class BlindDateControllerImpl extends AbstractController implements BlindDateController {

    private final BlindDateService blindDateService;

    public BlindDateControllerImpl(BlindDateService blindDateService) {
        super(ServiceType.BLIND);
        this.blindDateService = blindDateService;
    }

    @Override
    public Response<Set<BlindUserWithRequestStatusResponse>> blindUsersInfo(Pageable pageable) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindDateService.blindUsersInfo(userId, pageable));
    }

    @Override
    public Response<Void> sendJoinRequest(SendBlindRequest request) {
        Long fromUserId = 9L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        blindDateService.createJoinRequest(fromUserId, request.getToUserId());
        return success();
    }

    @Override
    public Response<Void> deleteJoinRequest(long blindRequestId) {
        blindDateService.deleteRequest(blindRequestId);
        return success();
    }

    @Override
    public Response<List<BlindDateResponse>> confirmOfMyRequest() {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindDateService.myRequest(userId).stream().collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public Response<List<BlindDateResponse>> confirmOfRequestToMe() {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindDateService.requestToMe(userId).stream().collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public Response<Void> acceptedRequest(long blindRequestId) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindDateService.acceptRequest(userId, blindRequestId);
        return success();
    }

    @Override
    public Response<Void> rejectRequest(long blindRequestId) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindDateService.rejectRequest(userId, blindRequestId);
        return success();
    }
}
