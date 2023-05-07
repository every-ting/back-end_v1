package com.ting.ting.configuration;

import com.ting.ting.repository.*;
import com.ting.ting.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class AppConfig {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMemberRequestRepository groupMemberRequestRepository;
    private final BlindRequestRepository blindRequestRepository;

    @Bean
    public GroupService groupService() {
        return new GroupServiceImpl(groupRepository, groupMemberRepository, groupMemberRequestRepository, userRepository);
    }

    @Bean
    public BlindRequestService blindRequestService() {
        return new BlindRequestServiceImpl(userRepository, blindRequestRepository);
    }
}
