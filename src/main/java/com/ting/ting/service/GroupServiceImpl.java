package com.ting.ting.service;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupMember;
import com.ting.ting.domain.GroupMemberRequest;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.MemberStatus;
import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.GroupMemberResponse;
import com.ting.ting.dto.response.GroupResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.repository.GroupMemberRepository;
import com.ting.ting.repository.GroupMemberRequestRepository;
import com.ting.ting.repository.GroupRepository;
import com.ting.ting.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public Page<GroupResponse> findAllGroups(Pageable pageable) {
        return groupRepository.findAll(pageable).map(GroupResponse::from);
    }

    @Override
    public Page<GroupResponse> findSuggestedGroupList(Pageable pageable) {
        // TODO : 같은 성별 이면서 내가 속한 팀이 아닌 팀 조회 구현
        return null;
    }

    @Override
    public Set<GroupResponse> findMyGroupList(Long userId) {
        User member = loadUserByUserId(userId);

        return groupMemberRepository.findAllGroupByMemberAndStatusActive(member).stream().map(GroupResponse::from).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<GroupMemberResponse> findGroupMemberList(Long groupId) {
        Group group = loadGroupByGroupId(groupId);

        return groupMemberRepository.findAllByGroup(group).stream().map(GroupMemberResponse::from).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public GroupResponse saveGroup(Long userId, GroupRequest request) {
        User leader = loadUserByUserId(userId);

        groupRepository.findByGroupName(request.getGroupName()).ifPresent(it -> {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("Group whose name is (%s) already exists", request.getGroupName()));
        });

        Group group = groupRepository.save(request.toEntity());
        groupMemberRepository.save(GroupMember.of(group, leader, MemberStatus.ACTIVE, MemberRole.LEADER));

        return GroupResponse.from(group);
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

    @Override
    public Set<GroupMemberResponse> changeGroupLeader(long groupId, long leaderId, long memberId) {
        Group group = loadGroupByGroupId(groupId);
        User leader = loadUserByUserId(leaderId);
        User member = loadUserByUserId(memberId);

        GroupMember groupMemberInfoOfLeader = groupMemberRepository.findByGroupAndMemberAndStatusActive(group, leader).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not a member of Group(id: %d)", leaderId, groupId))
        );
        GroupMember groupMemberInfoOfMember = groupMemberRepository.findByGroupAndMemberAndStatusActive(group, member).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not a member of Group(id: %d)", memberId, groupId))
        );

        if (!groupMemberInfoOfLeader.getRole().equals(MemberRole.LEADER)) {
            throwException(ErrorCode.INVALID_PERMISSION, String.format("User(id: %d) is not the leader of Group(id: %d)", leaderId, groupId));
        }

        groupMemberInfoOfLeader.setRole(MemberRole.MEMBER);
        groupMemberInfoOfMember.setRole(MemberRole.LEADER);

        return groupMemberRepository.saveAllAndFlush(List.of(groupMemberInfoOfLeader, groupMemberInfoOfMember)).stream().map(GroupMemberResponse::from).collect(Collectors.toUnmodifiableSet());
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
