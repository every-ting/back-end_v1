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
import com.ting.ting.util.QRCodeGenerator;
import com.ting.ting.util.S3StorageManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Transactional
@Component
public class GroupServiceImpl extends AbstractService implements GroupService {

    @Value("${server.url}")
    private String serverUrl;

    private final GroupRepository groupRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMemberRequestRepository groupMemberRequestRepository;
    private final GroupDateRepository groupDateRepository;
    private final GroupDateRequestRepository groupDateRequestRepository;
    private final UserRepository userRepository;
    private final S3StorageManager s3StorageManager;

    public GroupServiceImpl(GroupRepository groupRepository, GroupInvitationRepository groupInvitationRepository, GroupMemberRepository groupMemberRepository, GroupMemberRequestRepository groupMemberRequestRepository, GroupDateRepository groupDateRepository, GroupDateRequestRepository groupDateRequestRepository, UserRepository userRepository, S3StorageManager s3StorageManager) {
        super(ServiceType.GROUP_MEETING);
        this.groupRepository = groupRepository;
        this.groupInvitationRepository = groupInvitationRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupMemberRequestRepository = groupMemberRequestRepository;
        this.groupDateRepository = groupDateRepository;
        this.groupDateRequestRepository = groupDateRequestRepository;
        this.userRepository = userRepository;
        this.s3StorageManager = s3StorageManager;
    }

    @Override
    public Page<GroupResponse> findAllGroups(Pageable pageable) {
        return groupRepository.findAll(pageable).map(GroupResponse::from);
    }

    @Override
    public Page<GroupWithRequestStatusResponse> findSuggestedSameGenderGroupList(Long userId, Pageable pageable) {
        User user = loadUserByUserId(userId);

        return groupRepository.findAllSuggestedGroupWithRequestStatusByUserAndGender(user, user.getGender(), pageable).map(GroupWithRequestStatusResponse::from);
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

        GroupMember memberRecordOfUser = groupMemberRepository.findByGroupAndMemberAndStatus(group, member, MemberStatus.ACTIVE).orElseThrow(() ->
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

        if (groupMemberRepository.existsByMemberAndStatusAndRole(newLeader, MemberStatus.ACTIVE, MemberRole.LEADER)) {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("User(id: %d) is already a leader in another group", newLeader.getId()));
        }

        GroupMember memberRecordOfLeader = groupMemberRepository.findByGroupAndMemberAndStatusAndRole(group, leader, MemberStatus.ACTIVE, MemberRole.LEADER).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) is not the leader of Group(id: %d)", userIdOfLeader, groupId))
        );
        GroupMember memberRecordOfNewLeader = groupMemberRepository.findByGroupAndMemberAndStatusAndRole(group, newLeader, MemberStatus.ACTIVE, MemberRole.MEMBER).orElseThrow(() ->
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

        GroupMember created = groupMemberRepository.save(GroupMember.of(groupMemberRequest.getGroup(), groupMemberRequest.getUser(), MemberStatus.ACTIVE, MemberRole.MEMBER));
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
    public GroupInvitationResponse createGroupMemberInvitation(long groupId, long userIdOfLeader) {
        Group group = loadGroupByGroupId(groupId);
        User leader = loadUserByUserId(userIdOfLeader);

        throwIfUserIsNotTheLeaderOfGroup(leader, group);

        Long currentNumberOfMembers = groupMemberRepository.countByGroup(group);

        if (currentNumberOfMembers >= group.getMemberSizeLimit()) {
            throwException(ErrorCode.REACHED_MEMBERS_SIZE_LIMIT, String.format("Maximum Group(id: %d) capacity of %d members reached", groupId, group.getMemberSizeLimit()));
        }

        // 초대 번호를 이용한 초대 qr 생성
        String invitationCode = groupId + UUID.randomUUID().toString();
        byte[] qrImageBytes = generateGroupInvitationQRCodeBytes(groupId, invitationCode);
        String qrImageUrl = uploadGroupInvitationQRCodeToStorage(groupId, invitationCode, qrImageBytes);
        groupMemberRepository.save(GroupMember.of(group, null, MemberStatus.PENDING, MemberRole.MEMBER));
        GroupInvitation created = groupInvitationRepository.save(GroupInvitation.of(group, invitationCode, qrImageUrl));

        return GroupInvitationResponse.from(created);
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

    private byte[] generateGroupInvitationQRCodeBytes(long groupId, String invitationCode) {
        // TODO : linkedUrl에 front url이 담겨야 함. 추후에 front url 확정되면 수정
        String linkedUrl = serverUrl + "/groups/" + groupId + "/members/invitations/" + invitationCode;
        byte[] qrImage = null;

        try {
            qrImage = QRCodeGenerator.generateQRCodeImageBytes(linkedUrl);
        } catch (Exception e) {
            throwException(ErrorCode.QR_GENERATOR_ERROR);
        }

        return qrImage;
    }

    private String uploadGroupInvitationQRCodeToStorage(long groupId, String imageFileName, byte[] qrImageBytes) {
        String qrImageKey = "qr/" + groupId + "/" + imageFileName + ".png";
        return s3StorageManager.uploadByteArrayToS3WithKey(qrImageBytes, qrImageKey);
    }

    private void throwIfUserIsNotTheLeaderOfGroup(User leader, Group group) {
        if (!groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(group, leader, MemberStatus.ACTIVE, MemberRole.LEADER)) {
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
