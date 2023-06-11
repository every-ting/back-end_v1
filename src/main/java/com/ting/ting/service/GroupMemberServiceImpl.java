package com.ting.ting.service;

import com.ting.ting.domain.*;
import com.ting.ting.domain.constant.LikeStatus;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.domain.custom.GroupWithMemberCount;
import com.ting.ting.dto.response.GroupMemberRequestResponse;
import com.ting.ting.dto.response.GroupMemberResponse;
import com.ting.ting.dto.response.JoinableGroupResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.repository.*;
import com.ting.ting.util.IdealPhotoManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Component
public class GroupMemberServiceImpl extends AbstractService implements GroupMemberService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupLikeToJoinRepository groupLikeToJoinRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMemberRequestRepository groupMemberRequestRepository;
    private final IdealPhotoManager idealPhotoManager;

    public GroupMemberServiceImpl(UserRepository userRepository, GroupRepository groupRepository, GroupLikeToJoinRepository groupLikeToJoinRepository, GroupMemberRepository groupMemberRepository, GroupMemberRequestRepository groupMemberRequestRepository, IdealPhotoManager idealPhotoManager) {
        super(ServiceType.GROUP_MEETING);
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupLikeToJoinRepository = groupLikeToJoinRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMemberRequestRepository = groupMemberRequestRepository;
        this.idealPhotoManager = idealPhotoManager;
    }

    @Override
    public void saveJoinRequest(long groupId) {
        Group group = loadGroupByGroupId(groupId);
        User user = loadUserByUserId(getCurrentUserId());

        if (group.getGender() != user.getGender()) {
            throwException(ErrorCode.GENDER_NOT_MATCH, String.format("Gender values of Group(id:%d) and User(id:%d) do not match", groupId, user.getId()));
        }

        if (group.isJoinable() == false) {
            throwException(ErrorCode.REACHED_MEMBERS_SIZE_LIMIT, String.format("Maximum Group(id: %d) capacity of %d members reached", groupId, group.getMemberSizeLimit()));
        }

        groupMemberRequestRepository.findByGroupAndUser(group, user).ifPresent(it -> {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("User(id:%d) already requested to join the Group(id:%d)", user.getId(), groupId));
        });

        if (groupMemberRepository.existsByGroupAndMember(group, user)) {
            throwException(ErrorCode.ALREADY_JOINED, String.format("User(id: %d) already joined to Group(id: %d)", user.getId(), groupId));
        }

        groupMemberRequestRepository.save(GroupMemberRequest.of(group, user));
    }

    @Override
    public void deleteJoinRequest(long groupId) {
        groupMemberRequestRepository.deleteByGroup_IdAndUser_Id(groupId, getCurrentUserId());
    }

    @Override
    public void deleteGroupMember(long groupId) {
        Group group = loadGroupByGroupId(groupId);
        User member = loadUserByUserId(getCurrentUserId());

        GroupMember memberRecordOfUser = groupMemberRepository.findByGroupAndMember(group, member).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not a member of the Group(id: %d)", member.getId(), group))
        );

        // 팀에서 나가려는 유저가 그 팀의 리더라면
        if (memberRecordOfUser.getRole().equals(MemberRole.LEADER)) {
            GroupMember memberRecordOfNewLeader = loadAvailableMemberAsNewLeaderInGroup(group);
            groupMemberRepository.delete(memberRecordOfUser);
            memberRecordOfNewLeader.setRole(MemberRole.LEADER);
            return;
        }

        groupMemberRepository.delete(memberRecordOfUser);
    }

    @Override
    public Set<GroupMemberResponse> changeGroupLeader(long groupId, long userIdOfNewLeader) {
        if (getCurrentUserId() == userIdOfNewLeader) {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("User(id: %d) is unable to transfer ownership to themselves.", getCurrentUserId()));
        }

        Group group = loadGroupByGroupId(groupId);
        User leader = loadUserByUserId(getCurrentUserId());
        User newLeader = loadUserByUserId(userIdOfNewLeader);

        if (groupMemberRepository.existsByMemberAndRole(newLeader, MemberRole.LEADER)) {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("User(id: %d) is already a leader in another group", newLeader.getId()));
        }

        GroupMember memberRecordOfLeader = groupMemberRepository.findByGroupAndMemberAndRole(group, leader, MemberRole.LEADER).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not the leader of Group(id: %d)", leader.getId(), groupId))
        );
        GroupMember memberRecordOfNewLeader = groupMemberRepository.findByGroupAndMemberAndRole(group, newLeader, MemberRole.MEMBER).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not a member of Group(id: %d)", userIdOfNewLeader, groupId))
        );

        memberRecordOfLeader.setRole(MemberRole.MEMBER);
        memberRecordOfNewLeader.setRole(MemberRole.LEADER);

        return groupMemberRepository.saveAllAndFlush(List.of(memberRecordOfLeader, memberRecordOfNewLeader)).stream().map(GroupMemberResponse::from).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Page<JoinableGroupResponse> findUserJoinRequestList(Pageable pageable) {
        User user = loadUserByUserId(getCurrentUserId());

        Page<GroupMemberRequest> userRequestsToJoin = groupMemberRequestRepository.findAllByUser(user, pageable);
        List<Long> requestedGroupIds = userRequestsToJoin.stream().map(GroupMemberRequest::getGroup).map(Group::getId).collect(Collectors.toUnmodifiableList());

        // 요청한 기록이 없는 경우는 바로 return
        if (userRequestsToJoin.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, userRequestsToJoin.getTotalElements());
        }

        Set<Long> likedGroupIds = groupLikeToJoinRepository.findAllByFromUser(user).stream().map(GroupLikeToJoin::getToGroup).map(Group::getId).collect(Collectors.toUnmodifiableSet());
        List<GroupWithMemberCount> myRequestedGroupsWithMemberCount = groupRepository.findAllWithMemberCountByIdIn(requestedGroupIds);

        List<JoinableGroupResponse> pendingJoinGroupResponses = myRequestedGroupsWithMemberCount.stream()
                .map(pendingJoinGroup -> JoinableGroupResponse.from(pendingJoinGroup, RequestStatus.PENDING, likedGroupIds.contains(pendingJoinGroup.getId()) ? LikeStatus.LIKED : LikeStatus.NOT_LIKED))
                .collect(Collectors.toUnmodifiableList());

        return new PageImpl<>(pendingJoinGroupResponses, pageable, userRequestsToJoin.getTotalElements());
    }

    @Override
    public Set<GroupMemberResponse> findGroupMemberList(Long groupId) {
        Group group = loadGroupByGroupId(groupId);

        return groupMemberRepository.findAllByGroup(group).stream().map(GroupMemberResponse::from).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<GroupMemberRequestResponse> findMemberJoinRequest(long groupId) {
        Group group = loadGroupByGroupId(groupId);
        User leader = loadUserByUserId(getCurrentUserId());

        throwIfUserIsNotTheLeaderOfGroup(leader, group);

        return groupMemberRequestRepository.findByGroup(group).stream().map(GroupMemberRequestResponse::from).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public GroupMemberResponse acceptMemberJoinRequest(long groupMemberRequestId) {
        User leader = loadUserByUserId(getCurrentUserId());
        GroupMemberRequest groupMemberRequest = groupMemberRequestRepository.findById(groupMemberRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("GroupMemberRequest(id: %d) not found", groupMemberRequestId))
        );
        Group group = groupMemberRequest.getGroup();
        User user = groupMemberRequest.getUser();

        // validate group member
        if (groupMemberRepository.existsByGroupAndMember(group, user)) {
            groupMemberRequestRepository.delete(groupMemberRequest);
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("User(id: %d) is already a member of Group(id: %d)", groupMemberRequest.getUser().getId(), groupMemberRequest.getGroup().getId()));
        }

        // validate group size
        Long actualNumOfMembers = groupMemberRepository.countByGroup(group);
        if (actualNumOfMembers >= group.getMemberSizeLimit()) {
            throwException(ErrorCode.REACHED_MEMBERS_SIZE_LIMIT, String.format("Maximum Group(id: %d) capacity of %d members reached", groupMemberRequest.getGroup().getId(), groupMemberRequest.getGroup().getMemberSizeLimit()));
        }

        throwIfUserIsNotTheLeaderOfGroup(leader, group);

        // save group member
        GroupMember created = groupMemberRepository.save(GroupMember.of(group, user, MemberRole.MEMBER));
        groupMemberRequestRepository.delete(groupMemberRequest);

        // update group ideal photo
        if (user.getIdealPhoto() != null || !user.getIdealPhoto().isEmpty()) {
            group.setIdealPhoto(idealPhotoManager.mixIdealPhotos(group.getIdealPhoto(), user.getIdealPhoto()).getImageURL());
        }

        if (actualNumOfMembers + 1 >= groupMemberRequest.getGroup().getMemberSizeLimit()) {
            groupMemberRequest.getGroup().setJoinable(false);
        }

        return GroupMemberResponse.from(created);
    }

    @Override
    public void rejectMemberJoinRequest(long groupMemberRequestId) {
        User leader = loadUserByUserId(getCurrentUserId());
        GroupMemberRequest groupMemberRequest = groupMemberRequestRepository.findById(groupMemberRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("GroupMemberRequest(id: %d) not found", groupMemberRequestId))
        );

        throwIfUserIsNotTheLeaderOfGroup(leader, groupMemberRequest.getGroup());

        groupMemberRequestRepository.delete(groupMemberRequest);
    }

    private void throwIfUserIsNotTheLeaderOfGroup(User leader, Group group) {
        if (!groupMemberRepository.existsByGroupAndMemberAndRole(group, leader, MemberRole.LEADER)) {
            throwException(ErrorCode.INVALID_PERMISSION, String.format("User(id: %d) is not the leader of Group(id: %d)", leader.getId(), group.getId()));
        }
    }

    private GroupMember loadAvailableMemberAsNewLeaderInGroup(Group group) {
        List<GroupMember> members = groupMemberRepository.findAvailableMemberAsALeaderInGroup(group, PageRequest.of(0, 1));
        if (members.isEmpty()) {
            throwException(ErrorCode.NO_AVAILABLE_MEMBER_AS_LEADER);
        }

        return members.get(0);
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
