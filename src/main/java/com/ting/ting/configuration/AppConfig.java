package com.ting.ting.configuration;

import com.ting.ting.repository.*;
import com.ting.ting.service.BlindService;
import com.ting.ting.service.BlindServiceImpl;
import com.ting.ting.service.GroupService;
import com.ting.ting.service.GroupServiceImpl;
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
    private final GroupDateRepository groupDateRepository;
    private final GroupDateRequestRepository groupDateRequestRepository;
    private final BlindRequestRepository blindRequestRepository;
    private final BlindDateRepository blindDateRepository;

    @Bean
    public GroupService groupService() {
        return new GroupServiceImpl(groupRepository, groupMemberRepository, groupMemberRequestRepository, groupDateRepository, groupDateRequestRepository, userRepository);
    }

    @Bean
    public BlindService blindService() {
        return new BlindServiceImpl(userRepository, blindRequestRepository, blindDateRepository);
    }
}
