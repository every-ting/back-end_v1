package com.ting.ting.controller;

import com.ting.ting.dto.response.BlindLikeResponse;
import com.ting.ting.dto.response.BlindRequestWithFromAndToResponse;
import com.ting.ting.dto.response.BlindUserWithRequestStatusAndLikeStatusResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.BlindLikeService;
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
    private final BlindLikeService blindLikeService;

    public BlindDateControllerImpl(BlindService blindService, BlindLikeService blindLikeService) {
        super(ServiceType.BLIND);
        this.blindService = blindService;
        this.blindLikeService = blindLikeService;
    }

    @Override
    public Response<Page<BlindUserWithRequestStatusAndLikeStatusResponse>> getBlindUsersInfo(Pageable pageable) {
        return success(blindService.blindUsersInfo(pageable));
    }

    @Override
    public Response<Void> sendJoinRequest(long toUserId) {
        blindService.createJoinRequest(toUserId);
        return success();
    }

    @Override
    public Response<Void> deleteJoinRequest(long toUserId) {
        blindService.deleteRequestById(toUserId);
        return success();
    }

    @Override
    public Response<BlindRequestWithFromAndToResponse> getBlindRequest() {
        return success(blindService.getBlindRequest());
    }

    @Override
    public Response<Void> acceptRequest(long blindRequestId) {
        blindService.acceptRequest(blindRequestId);
        return success();
    }

    @Override
    public Response<Void> rejectRequest(long blindRequestId) {
        blindService.rejectRequest(blindRequestId);
        return success();
    }

    @Override
    public Response<Void> sendJoinLiked(long toUserId) {
        blindLikeService.createJoinLiked(toUserId);
        return success();
    }

    @Override
    public Response<Void> deleteJoinLiked(long toUserId) {
        blindLikeService.deleteLikedByFromUserIdAndToUserId(toUserId);
        return success();
    }

    @Override
    public Response<Set<BlindLikeResponse>> getBlindLiked() {
        return success(blindLikeService.getBlindLike());
    }
}
