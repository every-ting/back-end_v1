package com.ting.ting.service;

import com.ting.ting.domain.*;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.LikeStatus;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.domain.custom.GroupWithMemberCount;
import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.*;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.repository.*;
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
public class GroupServiceImpl extends AbstractService implements GroupService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMemberRequestRepository groupMemberRequestRepository;
    private final GroupDateRepository groupDateRepository;
    private final GroupDateRequestRepository groupDateRequestRepository;
    private final GroupLikeToJoinRepository groupLikeToJoinRepository;
    private final GroupLikeToDateRepository groupLikeToDateRepository;

    public GroupServiceImpl(UserRepository userRepository, GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, GroupMemberRequestRepository groupMemberRequestRepository, GroupDateRepository groupDateRepository, GroupDateRequestRepository groupDateRequestRepository,
                            GroupLikeToJoinRepository groupLikeToJoinRepository, GroupLikeToDateRepository groupLikeToDateRepository) {
        super(ServiceType.GROUP_MEETING);
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMemberRequestRepository = groupMemberRequestRepository;
        this.groupDateRepository = groupDateRepository;
        this.groupDateRequestRepository = groupDateRequestRepository;
        this.groupLikeToJoinRepository = groupLikeToJoinRepository;
        this.groupLikeToDateRepository = groupLikeToDateRepository;
    }

    @Override
    public Page<GroupResponse> findAllGroups(Pageable pageable) {
        return groupRepository.findAllWithMemberCount(pageable).map(GroupResponse::from);
    }

    @Override
    public Page<GroupWithStatusResponse> findJoinableSameGenderGroupList(Long userId, Pageable pageable) {
        User user = loadUserByUserId(userId);

        Page<GroupWithMemberCount> joinableSameGenderGroups = groupRepository.findAllJoinableGroupWithMemberCountByGenderAndIsJoinableAndNotGroupMembers_Member(user.getGender(), true, user, pageable);
        Set<Long> myPendingJoinGroupIds = groupMemberRequestRepository.findAllGroupByUser(user).stream().map(Group::getId).collect(Collectors.toUnmodifiableSet());
        Set<Long> myLikeGroupIds = groupLikeToJoinRepository.findAllToGroupByFromUser(user).stream().map(Group::getId).collect(Collectors.toUnmodifiableSet());

        List<GroupWithStatusResponse> groupWithStatusResponses = joinableSameGenderGroups.stream()
                .map(joinableSameGenderGroup -> {
                    GroupWithStatusResponse response = GroupWithStatusResponse.from(joinableSameGenderGroup, RequestStatus.EMPTY, null);

                    if (myPendingJoinGroupIds.contains(joinableSameGenderGroup.getId())) {
                        response.setRequestStatus(RequestStatus.PENDING);
                    }

                    if (myLikeGroupIds.contains(joinableSameGenderGroup.getId())) {
                        response.setLikeStatus(LikeStatus.LIKED);
                    } else {
                        response.setLikeStatus(LikeStatus.NOT_LIKED);
                    }

                    return response;
        }).collect(Collectors.toList());

        return new PageImpl<>(groupWithStatusResponses, pageable, joinableSameGenderGroups.getTotalElements());
    }

    @Override
    public Page<DateableGroupResponse> findDateableOppositeGenderGroupList(Long groupId, Long userId, Pageable pageable) {
        Group group = loadGroupByGroupId(groupId);
        User member = loadUserByUserId(userId);

        GroupMember memberRecordOfUser = groupMemberRepository.findByGroupAndMember(group, member).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not a member of the Group(id: %d)", userId, group))
        );

        Page<Group> oppositeGenderGroups = groupRepository.findAllByGenderAndIsJoinableAndIsMatchedAndMemberSizeLimit(group.getGender().getOpposite(), false, false, group.getMemberSizeLimit(), pageable);
        List<Group> oppositeGenderGroupsWithMemberInfo = groupRepository.findAllWithMembersInfoByIdIn(oppositeGenderGroups.stream().map(Group::getId).collect(Collectors.toList()));
        Set<Long> likedGroupIds = groupLikeToDateRepository.findAllByFromGroupMember(memberRecordOfUser).stream().map(GroupLikeToDate::getToGroup).map(Group::getId).collect(Collectors.toUnmodifiableSet());

        List<DateableGroupResponse> dateableGroupResponses = oppositeGenderGroupsWithMemberInfo.stream()
                .map(oppositeGenderGroup -> {
                    LikeStatus likeStatus = likedGroupIds.contains(oppositeGenderGroup.getId()) ? LikeStatus.LIKED : LikeStatus.NOT_LIKED;
                    return DateableGroupResponse.from(oppositeGenderGroup, likeStatus);
        }).collect(Collectors.toList());

        return new PageImpl<>(dateableGroupResponses, pageable, oppositeGenderGroups.getTotalElements());
    }

    @Override
    public Set<MyGroupResponse> findMyGroupList(Long userId) {
        User user = loadUserByUserId(userId);

        return groupMemberRepository.findGroupWithMemberCountAndRoleByMember(user).stream().map(MyGroupResponse::from).collect(Collectors.toUnmodifiableSet());
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
        groupMemberRepository.save(GroupMember.of(group, leader, MemberRole.LEADER));

        return GroupResponse.from(group);
    }

    @Override
    public void saveJoinRequest(long groupId, long userId) {
        Group group = loadGroupByGroupId(groupId);
        User user = loadUserByUserId(userId);

        if (group.getGender() != user.getGender()) {
            throwException(ErrorCode.GENDER_NOT_MATCH, String.format("Gender values of Group(id:%d) and User(id:%d) do not match", groupId, userId));
        }

        if (group.isJoinable() == false) {
            throwException(ErrorCode.REACHED_MEMBERS_SIZE_LIMIT, String.format("Maximum Group(id: %d) capacity of %d members reached", groupId, group.getMemberSizeLimit()));
        }

        groupMemberRequestRepository.findByGroupAndUser(group, user).ifPresent(it -> {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("User(id:%d) already requested to join the Group(id:%d)", userId, groupId));
        });

        if (groupMemberRepository.existsByGroupAndMember(group, user)) {
            throwException(ErrorCode.ALREADY_JOINED, String.format("User(id: %d) already joined to Group(id: %d)", userId, groupId));
        }

        groupMemberRequestRepository.save(GroupMemberRequest.of(group, user));
    }

    @Override
    public void deleteJoinRequest(long groupId, long userId) {
        groupMemberRequestRepository.deleteByGroup_IdAndUser_Id(groupId, userId);
    }

    @Override
    public void deleteGroupMember(long groupId, long userId) {
        Group group = loadGroupByGroupId(groupId);
        User member = loadUserByUserId(userId);

        GroupMember memberRecordOfUser = groupMemberRepository.findByGroupAndMember(group, member).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not a member of the Group(id: %d)", userId, group))
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
    public Set<GroupMemberResponse> changeGroupLeader(long groupId, long userIdOfLeader, long userIdOfNewLeader) {
        if (userIdOfLeader == userIdOfNewLeader) {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("User(id: %d) is unable to transfer ownership to themselves.", userIdOfLeader));
        }

        Group group = loadGroupByGroupId(groupId);
        User leader = loadUserByUserId(userIdOfLeader);
        User newLeader = loadUserByUserId(userIdOfNewLeader);

        if (groupMemberRepository.existsByMemberAndRole(newLeader, MemberRole.LEADER)) {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("User(id: %d) is already a leader in another group", newLeader.getId()));
        }

        GroupMember memberRecordOfLeader = groupMemberRepository.findByGroupAndMemberAndRole(group, leader, MemberRole.LEADER).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not the leader of Group(id: %d)", userIdOfLeader, groupId))
        );
        GroupMember memberRecordOfNewLeader = groupMemberRepository.findByGroupAndMemberAndRole(group, newLeader, MemberRole.MEMBER).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not a member of Group(id: %d)", userIdOfNewLeader, groupId))
        );

        memberRecordOfLeader.setRole(MemberRole.MEMBER);
        memberRecordOfNewLeader.setRole(MemberRole.LEADER);

        return groupMemberRepository.saveAllAndFlush(List.of(memberRecordOfLeader, memberRecordOfNewLeader)).stream().map(GroupMemberResponse::from).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<GroupMemberRequestResponse> findMemberJoinRequest(long groupId, long userIdOfLeader) {
        Group group = loadGroupByGroupId(groupId);
        User leader = loadUserByUserId(userIdOfLeader);

        throwIfUserIsNotTheLeaderOfGroup(leader, group);

        return groupMemberRequestRepository.findByGroup(group).stream().map(GroupMemberRequestResponse::from).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public GroupMemberResponse acceptMemberJoinRequest(long userIdOfLeader, long groupMemberRequestId) {
        User leader = loadUserByUserId(userIdOfLeader);
        GroupMemberRequest groupMemberRequest = groupMemberRequestRepository.findById(groupMemberRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("GroupMemberRequest(id: %d) not found", groupMemberRequestId))
        );

        if (groupMemberRepository.existsByGroupAndMember(groupMemberRequest.getGroup(), groupMemberRequest.getUser())) {
            groupMemberRequestRepository.delete(groupMemberRequest);
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("User(id: %d) is already a member of Group(id: %d)", groupMemberRequest.getUser().getId(), groupMemberRequest.getGroup().getId()));
        }

        Long actualNumOfMembers = groupMemberRepository.countByGroup(groupMemberRequest.getGroup());

        if (actualNumOfMembers >= groupMemberRequest.getGroup().getMemberSizeLimit()) {
            throwException(ErrorCode.REACHED_MEMBERS_SIZE_LIMIT, String.format("Maximum Group(id: %d) capacity of %d members reached", groupMemberRequest.getGroup().getId(), groupMemberRequest.getGroup().getMemberSizeLimit()));
        }

        throwIfUserIsNotTheLeaderOfGroup(leader, groupMemberRequest.getGroup());

        GroupMember created = groupMemberRepository.save(GroupMember.of(groupMemberRequest.getGroup(), groupMemberRequest.getUser(), MemberRole.MEMBER));
        groupMemberRequestRepository.delete(groupMemberRequest);

        if (actualNumOfMembers + 1 >= groupMemberRequest.getGroup().getMemberSizeLimit()) {
            groupMemberRequest.getGroup().setJoinable(false);
        }

        return GroupMemberResponse.from(created);
    }

    @Override
    public void rejectMemberJoinRequest(long userIdOfLeader, long groupMemberRequestId) {
        User leader = loadUserByUserId(userIdOfLeader);
        GroupMemberRequest groupMemberRequest = groupMemberRequestRepository.findById(groupMemberRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("GroupMemberRequest(id: %d) not found", groupMemberRequestId))
        );

        throwIfUserIsNotTheLeaderOfGroup(leader, groupMemberRequest.getGroup());

        groupMemberRequestRepository.delete(groupMemberRequest);
    }

    @Override
    public GroupDateRequestWithFromAndToResponse findAllGroupDateRequest(long groupId, long userIdOfLeader) {
        Group group = loadGroupByGroupId(groupId);
        User leader = loadUserByUserId(userIdOfLeader);

        throwIfUserIsNotTheLeaderOfGroup(leader, group);

        Set<GroupResponse> receivedGroupDateRequests = groupDateRequestRepository.findFromGroupByToGroup(group).stream().map(GroupResponse::from).collect(Collectors.toUnmodifiableSet());
        Set<GroupResponse> sentGroupDateRequests = groupDateRequestRepository.findToGroupByFromGroup(group).stream().map(GroupResponse::from).collect(Collectors.toUnmodifiableSet());

        return new GroupDateRequestWithFromAndToResponse(receivedGroupDateRequests, sentGroupDateRequests);
    }

    @Override
    public GroupDateRequestResponse saveGroupDateRequest(long userIdOfLeader, long fromGroupId, long toGroupId) {
        User leader = loadUserByUserId(userIdOfLeader);
        Group fromGroup = loadGroupByGroupId(fromGroupId);
        Group toGroup = loadGroupByGroupId(toGroupId);
        Group menGroup, womenGroup;

        if (fromGroup.getGender() == toGroup.getGender()) {
            throwException(ErrorCode.INVALID_REQUEST, String.format("The genders of fromGroup(id: %d) and toGroup(id: %d) are the same", fromGroupId, toGroupId));
        }

        if (groupDateRequestRepository.existsByFromGroupAndToGroup(fromGroup, toGroup)) {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("fromGroup(id:%d) has already requested a date match with toGroup(id:%d)", fromGroup, toGroup));
        }

        throwIfUserIsNotTheLeaderOfGroup(leader, fromGroup);

        if (leader.getGender().equals(Gender.MEN)) {
            menGroup = fromGroup;
            womenGroup = toGroup;
        } else {
            menGroup = toGroup;
            womenGroup = fromGroup;
        }

        if (groupDateRepository.existsByMenGroupOrWomenGroup(menGroup, womenGroup)) {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("GroupDate of fromGroup(id: %d) or toGroup(id: %d) already exists", fromGroupId, toGroupId));
        }

        return GroupDateRequestResponse.from(groupDateRequestRepository.save(GroupDateRequest.of(fromGroup, toGroup)));
    }

    @Override
    public void deleteGroupDateRequest(long userIdOfLeader, long fromGroupId, long toGroupId) {
        User leader = loadUserByUserId(userIdOfLeader);
        Group fromGroup = loadGroupByGroupId(fromGroupId);

        throwIfUserIsNotTheLeaderOfGroup(leader, fromGroup);

        groupDateRequestRepository.deleteByFromGroup_IdAndToGroup_Id(fromGroupId, toGroupId);
    }

    @Override
    public GroupDateResponse acceptGroupDateRequest(long userIdOfLeader, long groupDateRequestId) {
        User leader = loadUserByUserId(userIdOfLeader);
        Group menGroup, womenGroup;
        GroupDateRequest groupDateRequest = groupDateRequestRepository.findById(groupDateRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("GroupDateRequest(id: %d) not found", groupDateRequestId))
        );

        if (groupDateRequest.getFromGroup().getGender().equals(groupDateRequest.getToGroup().getGender())) {
            groupDateRequestRepository.delete(groupDateRequest);
            throwException(ErrorCode.INVALID_REQUEST, String.format("The genders of fromGroup(id: %d) and toGroup(id: %d) are the same", groupDateRequest.getFromGroup().getId(), groupDateRequest.getToGroup().getId()));
        }

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

        menGroup.setMatched(true);
        womenGroup.setMatched(true);

        GroupDate created = groupDateRepository.save(GroupDate.of(menGroup, womenGroup));
        groupDateRequestRepository.delete(groupDateRequest);
        groupDateRequestRepository.deleteByFromGroupAndToGroup(groupDateRequest.getToGroup(), groupDateRequest.getFromGroup()); // toGroup이 fromGroup에 요청한 적이 있다면, 그 기록도 삭제한다.

        return GroupDateResponse.from(created);
    }

    @Override
    public void rejectGroupDateRequest(long userIdOfLeader, long groupDateRequestId) {
        User leader = loadUserByUserId(userIdOfLeader);
        GroupDateRequest groupDateRequest = groupDateRequestRepository.findById(groupDateRequestId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("GroupDateRequest(id: %d) not found", groupDateRequestId))
        );

        throwIfUserIsNotTheLeaderOfGroup(leader, groupDateRequest.getToGroup());

        groupDateRequestRepository.delete(groupDateRequest);
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
