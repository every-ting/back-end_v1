package com.ting.ting.service;

import com.ting.ting.domain.*;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component
public class GroupLikeServiceImpl extends AbstractService implements GroupLikeService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupLikeToJoinRepository groupLikeToJoinRepository;
    private final GroupLikeToDateRepository groupLikeToDateRepository;

    public GroupLikeServiceImpl(UserRepository userRepository, GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, GroupLikeToJoinRepository groupLikeToJoinRepository,
                                GroupLikeToDateRepository groupLikeToDateRepository){
        super(ServiceType.GROUP_MEETING);
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupLikeToJoinRepository = groupLikeToJoinRepository;
        this.groupLikeToDateRepository = groupLikeToDateRepository;
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
