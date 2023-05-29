package com.ting.ting.service;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupLikeToJoin;
import com.ting.ting.domain.User;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.repository.GroupLikeToJoinRepository;
import com.ting.ting.repository.GroupRepository;
import com.ting.ting.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component
public class GroupLikeServiceImpl extends AbstractService implements GroupLikeService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupLikeToJoinRepository groupLikeToJoinRepository;

    public GroupLikeServiceImpl(UserRepository userRepository, GroupRepository groupRepository, GroupLikeToJoinRepository groupLikeToJoinRepository){
        super(ServiceType.GROUP_MEETING);
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupLikeToJoinRepository = groupLikeToJoinRepository;
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
