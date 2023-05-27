package com.ting.ting.configuration;

import com.ting.ting.repository.*;
import com.ting.ting.service.*;
import com.ting.ting.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class AppConfig {

    private final S3StorageManager s3StorageManager;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMemberRequestRepository groupMemberRequestRepository;
    private final GroupDateRepository groupDateRepository;
    private final GroupDateRequestRepository groupDateRequestRepository;
    private final GroupLikeToDateRepository groupLikeToDateRepository;
    private final GroupLikeToJoinRepository groupLikeToJoinRepository;
    private final BlindRequestRepository blindRequestRepository;
    private final BlindDateRepository blindDateRepository;
    private final BlindLikeRepository blindLikeRepository;

    @Bean
    public GroupService groupService() {
        return new GroupServiceImpl(groupRepository, groupMemberRepository, groupMemberRequestRepository, groupDateRepository, groupDateRequestRepository, groupLikeToDateRepository, groupLikeToJoinRepository, userRepository, s3StorageManager);
    }

    @Bean
    public GroupLikeService groupLikeService() {
        return new GroupLikeServiceImpl(userRepository, groupRepository, groupLikeToJoinRepository);
    }

    @Bean
    public BlindService blindService() {
        return new BlindServiceImpl(userRepository, blindRequestRepository, blindDateRepository, blindLikeRepository);
    }
}
