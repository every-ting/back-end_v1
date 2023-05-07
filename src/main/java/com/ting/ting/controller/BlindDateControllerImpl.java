package com.ting.ting.controller;

import com.ting.ting.dto.request.SendBlindRequest;
import com.ting.ting.dto.response.BlindRequestResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.BlindRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
    public Response<Page<BlindRequestResponse>> blindUsersInfo(@ParameterObject Pageable pageable) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindRequestService.blindUsersInfo(userId, pageable));
    }

    @Override
    public Response<Void> sendJoinRequest(@RequestBody SendBlindRequest request) {
        Long fromUserId = 9L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        blindRequestService.createJoinRequest(fromUserId, request.getToUserId());
        return success();
    }

    @Override
    public Response<Void> deleteJoinRequest(@PathVariable long blindRequestId) {
        blindRequestService.deleteRequest(blindRequestId);
        return success();
    }

    @Override
    public Response<List<BlindRequestResponse>> confirmOfMyRequest() {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindRequestService.myRequest(userId).stream().collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public Response<List<BlindRequestResponse>> confirmOfRequestToMe() {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(blindRequestService.requestToMe(userId).stream().collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public Response<Void> acceptedRequest(@PathVariable long blindRequestId) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindRequestService.acceptRequest(userId, blindRequestId);
        return success();
    }

    @Override
    public Response<Void> rejectRequest(@PathVariable long blindRequestId) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        blindRequestService.rejectRequest(userId, blindRequestId);
        return success();
    }
}
