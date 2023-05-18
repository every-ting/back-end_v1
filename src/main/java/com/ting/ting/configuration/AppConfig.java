package com.ting.ting.configuration;

import com.ting.ting.repository.*;
import com.ting.ting.service.BlindRequestService;
import com.ting.ting.service.BlindRequestServiceImpl;
import com.ting.ting.service.GroupService;
import com.ting.ting.service.GroupServiceImpl;
import com.ting.ting.util.S3StorageManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class AppConfig {

    private final S3StorageManager s3StorageManager;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMemberRequestRepository groupMemberRequestRepository;
    private final GroupDateRepository groupDateRepository;
    private final GroupDateRequestRepository groupDateRequestRepository;
    private final BlindRequestRepository blindRequestRepository;

    @Bean
    public GroupService groupService() {
        return new GroupServiceImpl(groupRepository, groupInvitationRepository, groupMemberRepository, groupMemberRequestRepository, groupDateRepository, groupDateRequestRepository, userRepository, s3StorageManager);
    }

    @Bean
    public BlindRequestService blindRequestService() {
        return new BlindRequestServiceImpl(userRepository, blindRequestRepository);
    }
}
