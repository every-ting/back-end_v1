package com.ting.ting.service;

import com.ting.ting.domain.*;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.domain.custom.GroupIdWithLikeCount;
import com.ting.ting.dto.response.DateableGroupResponse;
import com.ting.ting.dto.response.GroupDateRequestResponse;
import com.ting.ting.dto.response.GroupDateResponse;
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
import java.util.stream.Collectors;

@Transactional
@Component
public class GroupDateServiceImpl extends AbstractService implements GroupDateService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupLikeToDateRepository groupLikeToDateRepository;
    private final GroupDateRepository groupDateRepository;
    private final GroupDateRequestRepository groupDateRequestRepository;

    public GroupDateServiceImpl(UserRepository userRepository, GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, GroupLikeToDateRepository groupLikeToDateRepository, GroupDateRepository groupDateRepository, GroupDateRequestRepository groupDateRequestRepository) {
        super(ServiceType.GROUP_MEETING);
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupLikeToDateRepository = groupLikeToDateRepository;
        this.groupDateRepository = groupDateRepository;
        this.groupDateRequestRepository = groupDateRequestRepository;
    }

    @Override
    public Page<DateableGroupResponse> findGroupDateRequests(long groupId, long userId, Pageable pageable) {
        Group group = loadGroupByGroupId(groupId);
        User member = loadUserByUserId(userId);

        GroupMember memberRecordOfUser = groupMemberRepository.findByGroupAndMember(group, member).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not a member of the Group(id: %d)", userId, group))
        );

        Page<GroupDateRequest> dateRequests = groupDateRequestRepository.findAllByToGroup(group, pageable);
        List<Group> fromGroups = dateRequests.stream().map(GroupDateRequest::getFromGroup).collect(Collectors.toUnmodifiableList());

        // 과팅 요청이 없는 경우는 바로 return
        if (dateRequests.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, dateRequests.getTotalElements());
        }

        List<GroupIdWithLikeCount> fromGroupIdsWithLikeCount = groupLikeToDateRepository.findAllToGroupIdAndLikeCountByFromGroupMember_GroupAndToGroupIdsIn(group, fromGroups);
        Map<Long, Integer> fromGroupIdWithLikeCountMap = fromGroupIdsWithLikeCount.stream().collect(Collectors.toMap(GroupIdWithLikeCount::getGroupId, GroupIdWithLikeCount::getLikeCount));
        List<Group> fromGroupWithMembersInfo = groupRepository.findAllWithMembersInfoByIdIn(fromGroups.stream().map(Group::getId).collect(Collectors.toUnmodifiableList()));

        List<DateableGroupResponse> fromGroupResponse = fromGroupWithMembersInfo.stream()
                .map(fromGroup -> {
                    DateableGroupResponse response = DateableGroupResponse.from(fromGroup, null, null, fromGroupIdWithLikeCountMap.getOrDefault(fromGroup.getId(), 0));

                    // 멤버에게는 수락, 거절 버튼이 비활성화 되도록
                    if (memberRecordOfUser.getRole() == MemberRole.LEADER) {
                        response.setRequestStatus(RequestStatus.PENDING);
                    } else {
                        response.setRequestStatus(RequestStatus.DISABLED);
                    }

                    return response;
        }).collect(Collectors.toList());

        return new PageImpl<>(fromGroupResponse, pageable, dateRequests.getTotalElements());
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
