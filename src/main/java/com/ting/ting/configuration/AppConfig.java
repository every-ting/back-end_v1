package com.ting.ting.configuration;

import com.ting.ting.repository.*;
import com.ting.ting.service.*;
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
    private final BlindDateRepository blindDateRepository;

    @Bean
    public GroupService groupService() {
        return new GroupServiceImpl(userRepository, groupRepository, groupMemberRepository, groupMemberRequestRepository, groupDateRepository, groupDateRequestRepository);
    }

    @Bean
    public GroupInvitationService groupInvitationService() {
        return new GroupInvitationServiceImpl(userRepository, groupRepository, groupMemberRepository, groupInvitationRepository, s3StorageManager);
    }

    @Bean
    public BlindService blindService() {
        return new BlindServiceImpl(userRepository, blindRequestRepository, blindDateRepository);
    }
}
