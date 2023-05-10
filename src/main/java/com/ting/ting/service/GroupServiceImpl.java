package com.ting.ting.service;

import com.ting.ting.domain.*;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.MemberStatus;
import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.*;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.repository.*;
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
    private final GroupDateRepository groupDateRepository;
    private final GroupDateRequestRepository groupDateRequestRepository;
    private final UserRepository userRepository;

    public GroupServiceImpl(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, GroupMemberRequestRepository groupMemberRequestRepository, GroupDateRepository groupDateRepository, GroupDateRequestRepository groupDateRequestRepository, UserRepository userRepository) {
        super(ServiceType.GROUP_MEETING);
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMemberRequestRepository = groupMemberRequestRepository;
        this.groupDateRepository = groupDateRepository;
        this.groupDateRequestRepository = groupDateRequestRepository;
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

        return groupMemberRepository.findAllGroupByMemberAndStatus(member, MemberStatus.ACTIVE).stream().map(GroupResponse::from).collect(Collectors.toUnmodifiableSet());
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

        //TODO : 이미 팀원인 경우 -> 에러

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
    public Set<GroupMemberResponse> changeGroupLeader(long groupId, long leaderId, long newLeaderId) {
        Group group = loadGroupByGroupId(groupId);
        User leader = loadUserByUserId(leaderId);
        User newLeader = loadUserByUserId(newLeaderId);

        // leader와 newLeader가 실제 group에 포함되어 있는 유저인지 확인
        GroupMember memberRecordOfLeader = groupMemberRepository.findByGroupAndMemberAndStatus(group, leader, MemberStatus.ACTIVE).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not a member of Group(id: %d)", leaderId, groupId))
        );
        GroupMember memberRecordOfNewLeader = groupMemberRepository.findByGroupAndMemberAndStatus(group, newLeader, MemberStatus.ACTIVE).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not a member of Group(id: %d)", newLeaderId, groupId))
        );

        // leaderId를 가진 user가 group 멤버면서, 리더는 아닌 경우 -> 에러
        if (!memberRecordOfLeader.getRole().equals(MemberRole.LEADER)) {
            throwException(ErrorCode.INVALID_PERMISSION, String.format("User(id: %d) is not the leader of Group(id: %d)", leaderId, groupId));
        }

        memberRecordOfLeader.setRole(MemberRole.MEMBER);
        memberRecordOfNewLeader.setRole(MemberRole.LEADER);

        return groupMemberRepository.saveAllAndFlush(List.of(memberRecordOfLeader, memberRecordOfNewLeader)).stream().map(GroupMemberResponse::from).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<GroupMemberRequestResponse> findMemberJoinRequest(long groupId, long leaderId) {
        Group group = loadGroupByGroupId(groupId);
        User leader = loadUserByUserId(leaderId);

        throwIfUserIsNotTheLeaderOfGroup(leader, group);

        return groupMemberRequestRepository.findByGroup(group).stream().map(GroupMemberRequestResponse::from).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public GroupMemberResponse acceptMemberJoinRequest(long leaderId, long groupMemberRequestId) {
        User leader = loadUserByUserId(leaderId);
        GroupMemberRequest groupMemberRequest = groupMemberRequestRepository.findById(groupMemberRequestId).orElseThrow(() ->
            throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("GroupMemberRequest(id: %d) not found", groupMemberRequestId))
        );

        throwIfUserIsNotTheLeaderOfGroup(leader, groupMemberRequest.getGroup());

        GroupMember created = groupMemberRepository.save(GroupMember.of(groupMemberRequest.getGroup(), groupMemberRequest.getUser(), MemberStatus.ACTIVE, MemberRole.MEMBER));
        groupMemberRequestRepository.delete(groupMemberRequest);
        return GroupMemberResponse.from(created);
    }

    @Override
    public void rejectMemberJoinRequest(long leaderId, long groupMemberRequestId) {
        User leader = loadUserByUserId(leaderId);
        GroupMemberRequest groupMemberRequest = groupMemberRequestRepository.findById(groupMemberRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("GroupMemberRequest(id: %d) not found", groupMemberRequestId))
        );

        throwIfUserIsNotTheLeaderOfGroup(leader, groupMemberRequest.getGroup());

        groupMemberRequestRepository.delete(groupMemberRequest);
    }

    @Override
    public Set<GroupDateRequestResponse> findAllGroupDateRequest(long groupId, long leaderId) {
        Group group = loadGroupByGroupId(groupId);
        User leader = loadUserByUserId(leaderId);

        throwIfUserIsNotTheLeaderOfGroup(leader, group);

        return groupDateRequestRepository.findByToGroup(group).stream().map(GroupDateRequestResponse::from).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public GroupDateResponse acceptGroupDateRequest(long leaderId, long groupDateRequestId) {
        User leader = loadUserByUserId(leaderId);
        Group menGroup, womenGroup;
        GroupDateRequest groupDateRequest = groupDateRequestRepository.findById(groupDateRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("GroupDateRequest(id: %d) not found", groupDateRequestId))
        );

        throwIfUserIsNotTheLeaderOfGroup(leader, groupDateRequest.getToGroup());

        if (leader.getGender().equals(Gender.MEN)) {
            menGroup = groupDateRequest.getToGroup();
            womenGroup = groupDateRequest.getFromGroup();
        } else {
            menGroup = groupDateRequest.getFromGroup();
            womenGroup = groupDateRequest.getToGroup();
        }

        if (groupDateRepository.existsByMenGroupOrWomenGroup(menGroup, womenGroup)) {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("GroupDate of fromGroup(id: %d) or toGroup(id: %d) already exists", groupDateRequest.getFromGroup().getId(), groupDateRequest.getToGroup().getId()));
        }

        GroupDate created = groupDateRepository.save(GroupDate.of(menGroup, womenGroup));
        groupDateRequestRepository.delete(groupDateRequest);

        return GroupDateResponse.from(created);
    }

    @Override
    public void rejectGroupDateRequest(long leaderId, long groupDateRequestId) {
        User leader = loadUserByUserId(leaderId);
        GroupDateRequest groupDateRequest = groupDateRequestRepository.findById(groupDateRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("GroupDateRequest(id: %d) not found", groupDateRequestId))
        );

        throwIfUserIsNotTheLeaderOfGroup(leader, groupDateRequest.getToGroup());

        groupDateRequestRepository.delete(groupDateRequest);
    }

    private void throwIfUserIsNotTheLeaderOfGroup(User leader, Group group) {
        // 과팅 팀의 팀장을 조회
        GroupMember memberRecordOfLeader = groupMemberRepository.findByGroupAndRole(group, MemberRole.LEADER).orElseThrow(() -> {
            groupRepository.delete(group); // TODO : 팀장이 없는 팀은 일단 삭제하는 것으로 구현 -> 다른 멤버에게 팀장을 넘기는 로직으로 수정
            return throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("Group(id: %d) not found", group.getId()));
        });

        // 과팅 팀의 팀장과 leaderId를 가진 유저가 같은 사람이 아닌 경우 -> 에러
        if (!memberRecordOfLeader.getMember().equals(leader)) {
            throwException(ErrorCode.INVALID_PERMISSION, String.format("User(id: %d) is not the leader of Group(id: %d)", leader.getId(), group.getId()));
        }
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
