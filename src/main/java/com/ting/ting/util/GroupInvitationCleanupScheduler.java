package com.ting.ting.util;

import com.ting.ting.service.GroupInvitationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GroupInvitationCleanupScheduler {

    private final GroupInvitationService groupInvitationService;

    public GroupInvitationCleanupScheduler(GroupInvitationService groupInvitationService) {
        this.groupInvitationService = groupInvitationService;
    }

    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000) // 매일 실행 (24시간마다)
    public void cleanupExpiredInvitations() {
        groupInvitationService.cleanupExpiredInvitations();
    }
}
