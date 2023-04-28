package com.ting.ting.service;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.User;
import com.ting.ting.dto.GroupDto;
import com.ting.ting.repository.GroupRepository;
import com.ting.ting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public void saveGroup(GroupDto dto) {
        User user = userRepository.getReferenceById(1L);

        Group group = dto.toEntity(user);
        groupRepository.save(group);
    }
}
