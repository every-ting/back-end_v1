package com.ting.ting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
public class BlindRequestWithFromAndToResponse {

    private Set<BlindDateResponse> receivedBlindRequests;
    private Set<BlindDateResponse> sendBlindRequests;
}
