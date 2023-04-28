package com.ting.ting.service;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.User;
import com.ting.ting.dto.GroupDto;
import com.ting.ting.repository.GroupRepository;
import com.ting.ting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Transactional
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public Page<GroupDto> list(Pageable pageable) {
        return groupRepository.findAll(pageable).map(GroupDto::from);
    }

    public Set<GroupDto> myList(Long userId) {
        User user = userRepository.getReferenceById(userId);

        return user.getGroups().stream().map(GroupDto::from).collect(Collectors.toSet());
    }

    public void saveGroup(GroupDto dto) {
        User user = userRepository.getReferenceById(9L);

        Group group = dto.toEntity(user);
        groupRepository.save(group);
    }

    public void createJoinRequest(long groupId) {
        User user = userRepository.getReferenceById(groupId + 1);

        Group group = groupRepository.getReferenceById(groupId);
        group.addJoinRequests(user);
    }

    public void deleteJoinRequest(long groupId) {
        User user = userRepository.getReferenceById(groupId + 1);

        Group group = groupRepository.getReferenceById(groupId);
        group.removeJoinRequests(user);
    }
}
