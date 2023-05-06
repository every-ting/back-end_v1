package com.ting.ting.service;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupMemberRequest;
import com.ting.ting.domain.User;
import com.ting.ting.dto.GroupDto;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.exception.TingApplicationException;
import com.ting.ting.repository.GroupMemberRepository;
import com.ting.ting.repository.GroupMemberRequestRepository;
import com.ting.ting.repository.GroupRepository;
import com.ting.ting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Transactional
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMemberRequestRepository groupMemberRequestRepository;
    private final UserRepository userRepository;

    /**
     * 모든 팀 조회
     */
    public Page<GroupDto> findAllGroups(Pageable pageable) {
        return groupRepository.findAll(pageable).map(GroupDto::from);
    }

    /**
     * 같은 성별 이면서 내가 속한 팀이 아닌 팀 조회
     */
    public Page<GroupDto> findSuggestedGroupList(Pageable pageable) {
        // TODO : 같은 성별 이면서 내가 속한 팀이 아닌 팀 조회 구현
        return null;
    }

    /**
     * 내가 속한 팀 조회 - request status : ACCEPTED
     */
    public Set<GroupDto> findMyGroupList(Long userId) {
        User member = loadUserByUserId(userId);

        return groupMemberRepository.findGroupByMemberAndStatusAccepted(member).stream().map(GroupDto::from).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 그룹 생성
     */
    public GroupDto saveGroup(Long userId, GroupDto dto) {
        User leader = loadUserByUserId(userId);

        groupRepository.findByGroupName(dto.getGroupName()).ifPresent(it -> {
            throw new TingApplicationException(
                    ErrorCode.DUPLICATED_REQUEST, ServiceType.GROUP_MEETING, String.format("Group whose name is (%s) already exists", dto.getGroupName())
            );
        });

        Group group = dto.toEntity(leader);

        return GroupDto.from(groupRepository.save(group));
    }

    /**
     * 같은 성별인 팀에 요청
     */
    public void saveJoinRequest(long groupId, long userId) {
        Group group = loadGroupByGroupId(groupId);
        User user = loadUserByUserId(userId);

        if (group.getGender() != user.getGender()) {
            throw new TingApplicationException(
                    ErrorCode.GENDER_NOT_MATCH, ServiceType.GROUP_MEETING, String.format("Gender values of Group(id:%d) and User(id:%d) do not match", groupId, userId)
            );
        }

        groupMemberRequestRepository.findByGroupAndUser(group, user).ifPresent(it -> {
            throw new TingApplicationException(
                    ErrorCode.DUPLICATED_REQUEST, ServiceType.GROUP_MEETING, String.format("User(id:%d) already requested to join the Group(id:%d)", userId, groupId)
            );
        });


        groupMemberRequestRepository.save(GroupMemberRequest.of(group, user));
    }

    /**
     * 같은 성별인 팀에 했던 요청을 취소
     */
    public void deleteJoinRequest(long groupId, long userId) {
        groupMemberRequestRepository.deleteByGroup_IdAndUser_Id(groupId, userId);
    }

    private Group loadGroupByGroupId(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow(() ->
                new TingApplicationException(
                    ErrorCode.REQUEST_NOT_FOUND, ServiceType.GROUP_MEETING, String.format("Group(id: %d) not found", groupId)
                )
        );
    }

    private User loadUserByUserId(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new TingApplicationException(
                        ErrorCode.REQUEST_NOT_FOUND, ServiceType.GROUP_MEETING, String.format("User(id: %d) not found", userId)
                )
        );
    }
}
