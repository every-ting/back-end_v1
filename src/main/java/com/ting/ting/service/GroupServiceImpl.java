package com.ting.ting.service;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupMemberRequest;
import com.ting.ting.domain.User;
import com.ting.ting.dto.GroupDto;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.repository.GroupMemberRepository;
import com.ting.ting.repository.GroupMemberRequestRepository;
import com.ting.ting.repository.GroupRepository;
import com.ting.ting.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Transactional
@Component
public class GroupServiceImpl extends AbstractService implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMemberRequestRepository groupMemberRequestRepository;
    private final UserRepository userRepository;

    public GroupServiceImpl(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, GroupMemberRequestRepository groupMemberRequestRepository, UserRepository userRepository) {
        super(ServiceType.GROUP_MEETING);
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMemberRequestRepository = groupMemberRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<GroupDto> findAllGroups(Pageable pageable) {
        return groupRepository.findAll(pageable).map(GroupDto::from);
    }

    @Override
    public Page<GroupDto> findSuggestedGroupList(Pageable pageable) {
        // TODO : 같은 성별 이면서 내가 속한 팀이 아닌 팀 조회 구현
        return null;
    }

    @Override
    public Set<GroupDto> findMyGroupList(Long userId) {
        User member = loadUserByUserId(userId);

        return groupMemberRepository.findGroupByMemberAndStatusAccepted(member).stream().map(GroupDto::from).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public GroupDto saveGroup(Long userId, GroupDto dto) {
        User leader = loadUserByUserId(userId);

        groupRepository.findByGroupName(dto.getGroupName()).ifPresent(it -> {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("Group whose name is (%s) already exists", dto.getGroupName()));
        });

        Group group = dto.toEntity(leader);
        return GroupDto.from(groupRepository.save(group));
    }

    @Override
    public void saveJoinRequest(long groupId, long userId) {
        Group group = loadGroupByGroupId(groupId);
        User user = loadUserByUserId(userId);

        if (group.getGender() != user.getGender()) {
            throwException(ErrorCode.GENDER_NOT_MATCH, String.format("Gender values of Group(id:%d) and User(id:%d) do not match", groupId, userId));
        }

        groupMemberRequestRepository.findByGroupAndUser(group, user).ifPresent(it -> {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("User(id:%d) already requested to join the Group(id:%d)", userId, groupId));
        });

        groupMemberRequestRepository.save(GroupMemberRequest.of(group, user));
    }

    @Override
    public void deleteJoinRequest(long groupId, long userId) {
        groupMemberRequestRepository.deleteByGroup_IdAndUser_Id(groupId, userId);
    }

    private Group loadGroupByGroupId(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("Group(id: %d) not found", groupId))
        );
    }

    private User loadUserByUserId(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) not found", userId))
        );
    }
}
