package com.ting.ting.service;

import com.ting.ting.domain.*;
import com.ting.ting.domain.constant.LikeStatus;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.domain.custom.GroupIdWithLikeCount;
import com.ting.ting.domain.custom.GroupWithMemberCount;
import com.ting.ting.dto.response.DateableGroupResponse;
import com.ting.ting.dto.response.JoinableGroupResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Component
public class GroupLikeServiceImpl extends AbstractService implements GroupLikeService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMemberRequestRepository groupMemberRequestRepository;
    private final GroupDateRequestRepository groupDateRequestRepository;
    private final GroupLikeToJoinRepository groupLikeToJoinRepository;
    private final GroupLikeToDateRepository groupLikeToDateRepository;

    public GroupLikeServiceImpl(UserRepository userRepository, GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, GroupMemberRequestRepository groupMemberRequestRepository, GroupDateRequestRepository groupDateRequestRepository,
                                GroupLikeToJoinRepository groupLikeToJoinRepository, GroupLikeToDateRepository groupLikeToDateRepository){
        super(ServiceType.GROUP_MEETING);
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMemberRequestRepository = groupMemberRequestRepository;
        this.groupDateRequestRepository = groupDateRequestRepository;
        this.groupLikeToJoinRepository = groupLikeToJoinRepository;
        this.groupLikeToDateRepository = groupLikeToDateRepository;
    }

    @Override
    public Page<DateableGroupResponse> findGroupLikeToDateList(Long groupId, Long userId, Pageable pageable) {
        Group group = loadGroupByGroupId(groupId);
        User member = loadUserByUserId(userId);

        GroupMember memberRecordOfUser = groupMemberRepository.findByGroupAndMember(group, member).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not a member of the Group(id: %d)", userId, group))
        );

        Page<GroupIdWithLikeCount> idAndLikeCountOfGroupsLikeToDate = groupLikeToDateRepository.findAllToGroupIdAndLikeCountByFromGroupMember_Group(group, pageable);
        Map<Long, Integer> groupIdWithLikeCountMap = idAndLikeCountOfGroupsLikeToDate.stream().collect(Collectors.toMap(GroupIdWithLikeCount::getGroupId, GroupIdWithLikeCount::getLikeCount));

        // 찜한 기록이 없는 경우는 바로 return
        if (idAndLikeCountOfGroupsLikeToDate.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, idAndLikeCountOfGroupsLikeToDate.getTotalElements());
        }

        Set<Group> likedGroups = groupRepository.findAllWithMembersInfoByIdIn(idAndLikeCountOfGroupsLikeToDate.stream().map(GroupIdWithLikeCount::getGroupId).collect(Collectors.toList())).stream().collect(Collectors.toUnmodifiableSet());

        Set<Long> pendingDateGroupIds = groupDateRequestRepository.findAllByFromGroup(group).stream().map(GroupDateRequest::getToGroup).map(Group::getId).collect(Collectors.toUnmodifiableSet());
        Set<Long> myLikeGroupIds = groupLikeToDateRepository.findAllByFromGroupMember(memberRecordOfUser).stream().map(GroupLikeToDate::getToGroup).map(Group::getId).collect(Collectors.toUnmodifiableSet());

        List<DateableGroupResponse> likedDateableGroupResponses = likedGroups.stream()
                .map(likedGroup -> {
                    DateableGroupResponse response = DateableGroupResponse.from(likedGroup, RequestStatus.EMPTY, null, groupIdWithLikeCountMap.get(likedGroup.getId()));

                    if (likedGroup.isMatched() == true) {
                        response.setRequestStatus(RequestStatus.DISABLED);
                    } else if (pendingDateGroupIds.contains(likedGroup.getId())) {
                        response.setRequestStatus(RequestStatus.PENDING);
                    }

                    if (myLikeGroupIds.contains(likedGroup.getId())) {
                        response.setLikeStatus(LikeStatus.LIKED);
                    } else {
                        response.setLikeStatus(LikeStatus.NOT_LIKED);
                    }

                    return response;
        }).collect(Collectors.toList());

        return new PageImpl<>(likedDateableGroupResponses, pageable, idAndLikeCountOfGroupsLikeToDate.getTotalElements());
    }

    @Override
    public Page<JoinableGroupResponse> findGroupLikeToJoinList(Long userId, Pageable pageable) {
        User user = loadUserByUserId(userId);

        Page<GroupLikeToJoin> groupLikesToJoin = groupLikeToJoinRepository.findAllByFromUser(user, pageable);
        List<Long> likedGroupIds = groupLikesToJoin.stream().map(GroupLikeToJoin::getToGroup).map(Group::getId).collect(Collectors.toUnmodifiableList());
        List<GroupWithMemberCount> likedGroupsWithMemberCount = groupRepository.findAllWithMemberCountByIdIn(likedGroupIds);

        Set<Long> pendingJoinRequestGroupIds = groupMemberRequestRepository.findAllByUser(user).stream().map(GroupMemberRequest::getGroup).map(Group::getId).collect(Collectors.toUnmodifiableSet());

        List<JoinableGroupResponse> likedJoinableGroupResponses = likedGroupsWithMemberCount.stream()
                .map(likedGroup -> {
                    JoinableGroupResponse response = JoinableGroupResponse.from(likedGroup, RequestStatus.EMPTY, LikeStatus.LIKED);

                    if (likedGroup.isJoinable() == false) {
                        response.setRequestStatus(RequestStatus.DISABLED);
                    } else if (pendingJoinRequestGroupIds.contains(likedGroup.getId())) {
                        response.setRequestStatus(RequestStatus.PENDING);
                    }

                    return response;
        }).collect(Collectors.toList());

        return new PageImpl<>(likedJoinableGroupResponses, pageable, groupLikesToJoin.getTotalElements());
    }

    @Override
    public void createSameGenderGroupLike(Long toGroupId, Long fromUserId) {
        Group group = loadGroupByGroupId(toGroupId);
        User user = loadUserByUserId(fromUserId);

        if (group.getGender() != user.getGender()) {
            throwException(ErrorCode.GENDER_NOT_MATCH, String.format("Gender values of Group(id:%d) and User(id:%d) do not match", toGroupId, fromUserId));
        }

        if (groupLikeToJoinRepository.existsByFromUserAndToGroup(user, group)) {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("User(id: %d) already liked Group(id: %d)", fromUserId, toGroupId));
        }

        groupLikeToJoinRepository.save(GroupLikeToJoin.of(user, group));
    }

    @Override
    public void deleteSameGenderGroupLike(Long groupId, Long userId) {
        loadUserByUserId(userId); // 유저 검증

        groupLikeToJoinRepository.deleteByFromUser_IdAndToGroup_Id(userId, groupId);
    }

    @Override
    public void createOppositeGenderGroupLike(Long fromGroupId, Long toGroupId, Long userId) {
        Group fromGroup = loadGroupByGroupId(fromGroupId);
        Group toGroup = loadGroupByGroupId(toGroupId);
        User member = loadUserByUserId(userId);

        if (fromGroup.getGender() == toGroup.getGender()) {
            throwException(ErrorCode.INVALID_REQUEST, String.format("The genders of fromGroup(id: %d) and toGroup(id: %d) are the same", fromGroupId, toGroupId));
        }

        GroupMember memberRecordOfUser = groupMemberRepository.findByGroupAndMember(fromGroup, member).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not a member of the Group(id: %d)", userId, fromGroupId))
        );

        if (groupLikeToDateRepository.existsByFromGroupMemberAndToGroup(memberRecordOfUser, toGroup)) {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("User(id: %d) already liked Group(id: %d)", userId, toGroupId));
        }

        groupLikeToDateRepository.save(GroupLikeToDate.of(memberRecordOfUser, toGroup));
    }

    @Override
    public void deleteOppositeGenderGroupLike(Long fromGroupId, Long toGroupId, Long userId) {
        Group fromGroup = loadGroupByGroupId(fromGroupId);
        Group toGroup = loadGroupByGroupId(toGroupId);
        User member = loadUserByUserId(userId);

        GroupMember memberRecordOfUser = groupMemberRepository.findByGroupAndMember(fromGroup, member).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not a member of the Group(id: %d)", userId, fromGroupId))
        );

        groupLikeToDateRepository.deleteByFromGroupMemberAndToGroup(memberRecordOfUser, toGroup);
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
