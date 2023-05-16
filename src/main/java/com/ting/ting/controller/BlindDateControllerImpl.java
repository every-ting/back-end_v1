package com.ting.ting.controller;

import com.ting.ting.dto.request.SendBlindRequest;
import com.ting.ting.dto.response.BlindDateResponse;
import com.ting.ting.dto.response.BlindUserWithRequestStatusResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.BlindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class BlindDateControllerImpl extends AbstractController implements BlindDateController {

    private final BlindService blindService;

    public BlindDateControllerImpl(BlindService blindService) {
        super(ServiceType.BLIND);
        this.blindService = blindService;
    }

    @Override
    public Response<Page<BlindUserWithRequestStatusResponse>> blindUsersInfo(Pageable pageable) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindService.blindUsersInfo(userId, pageable));
    }

    @Override
    public Response<Void> sendJoinRequest(SendBlindRequest request) {
        Long fromUserId = 9L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        blindService.createJoinRequest(fromUserId, request.getToUserId());
        return success();
    }

    @Override
    public Response<Void> deleteJoinRequest(long blindRequestId) {
        blindService.deleteRequest(blindRequestId);
        return success();
    }

    @Override
    public Response<List<BlindDateResponse>> confirmOfMyRequest() {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindService.myRequest(userId).stream().collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public Response<List<BlindDateResponse>> confirmOfRequestToMe() {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindService.requestToMe(userId).stream().collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public Response<Void> acceptedRequest(long blindRequestId) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindService.acceptRequest(userId, blindRequestId);
        return success();
    }

    @Override
    public Response<Void> rejectRequest(long blindRequestId) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindService.rejectRequest(userId, blindRequestId);
        return success();
    }
}
