package com.ting.ting.service;

import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.exception.TingApplicationException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@AllArgsConstructor
public abstract class AbstractService {

    protected ServiceType serviceType;

    protected TingApplicationException throwException(ErrorCode errorCode) {
        throw new TingApplicationException(errorCode, serviceType);
    }

    protected TingApplicationException throwException(ErrorCode errorCode, String message) {
        throw new TingApplicationException(
            errorCode, serviceType, message
        );
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        return userId;
    }
}
