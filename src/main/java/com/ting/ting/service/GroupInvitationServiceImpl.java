package com.ting.ting.service;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.ting.ting.annotation.EnableFilters;
import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupInvitation;
import com.ting.ting.domain.GroupMember;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.MemberStatus;
import com.ting.ting.dto.response.GroupInvitationResponse;
import com.ting.ting.dto.response.GroupMemberResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.filter.FilterType;
import com.ting.ting.repository.GroupInvitationRepository;
import com.ting.ting.repository.GroupMemberRepository;
import com.ting.ting.repository.GroupRepository;
import com.ting.ting.repository.UserRepository;
import com.ting.ting.util.QRCodeGenerator;
import com.ting.ting.util.S3StorageManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;

@Transactional
@Component
public class GroupInvitationServiceImpl extends AbstractService implements GroupInvitationService {

    @Value("${server.url}")
    private String serverUrl;

    private static final String QR_CODE_PREFIX = "qr/group";
    private static final String IMAGE_EXTENSION = ".png";
    private static final Duration EXPIRATION_DURATION = Duration.ofDays(2);

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final S3StorageManager s3StorageManager;

    public GroupInvitationServiceImpl(UserRepository userRepository, GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, GroupInvitationRepository groupInvitationRepository, S3StorageManager s3StorageManager) {
        super(ServiceType.GROUP_MEETING);
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupInvitationRepository = groupInvitationRepository;
        this.s3StorageManager = s3StorageManager;
    }

    @Override
    @EnableFilters({
            FilterType.VALID_GROUP_MEMBER,
            FilterType.VALID_GROUP_INVITATION
    })
    public Set<GroupInvitationResponse> findAllGroupMemberInvitation(long groupId, long userIdOfLeader) {
        Group group = loadGroupByGroupId(groupId);
        User leader = loadUserByUserId(userIdOfLeader);

        throwIfUserIsNotTheLeaderOfGroup(leader, group);

        return groupInvitationRepository.findByGroupMember_Group(group).stream()
                .map(GroupInvitationResponse::from)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    @EnableFilters({
            FilterType.VALID_GROUP_MEMBER,
            FilterType.VALID_GROUP_INVITATION
    })
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

        // 초대될 user를 위한 GroupMember record 생성 + 만료시간 설정
        GroupMember pendingReservedMember = GroupMember.of(group, null, MemberStatus.PENDING, MemberRole.MEMBER);
        pendingReservedMember.setExpiredAt(now().plus(EXPIRATION_DURATION));
        GroupInvitation created = groupInvitationRepository.save(
                GroupInvitation.of(pendingReservedMember, invitationCode, qrImageUrl, now().plus(EXPIRATION_DURATION))
        );

        return GroupInvitationResponse.from(created);
    }

    @Override
    @EnableFilters({
            FilterType.VALID_GROUP_MEMBER,
            FilterType.VALID_GROUP_INVITATION
    })
    public void deleteGroupMemberInvitation(long groupId, long userIdOfLeader, long groupInvitationId) {
        Group group = loadGroupByGroupId(groupId);
        User leader = loadUserByUserId(userIdOfLeader);

        throwIfUserIsNotTheLeaderOfGroup(leader, group);

        GroupInvitation groupInvitation = groupInvitationRepository.findById(groupInvitationId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("GroupInvitation(id: %d) not found", groupInvitationId))
        );

        if (!groupInvitation.getGroupMember().getGroup().equals(group)) {
            throwException(ErrorCode.INVALID_REQUEST, String.format("GroupInvitation(id: %d) does not belong to Group(id: %d)", groupInvitationId, groupId));
        }

        GroupMember reservedGroupMemberRecord = groupInvitation.getGroupMember();

        groupInvitationRepository.delete(groupInvitation);
        groupMemberRepository.delete(reservedGroupMemberRecord);
        deleteGroupInvitationQRCodeFromStorage(getGroupInvitationQrImageKey(groupId, groupInvitation.getInvitationCode()));
    }

    @Override
    @EnableFilters({
            FilterType.VALID_GROUP_MEMBER,
            FilterType.VALID_GROUP_INVITATION
    })
    public GroupMemberResponse acceptGroupMemberInvitation(long groupId, long userId, String invitationCode) {
        Group group = loadGroupByGroupId(groupId);
        User invitedUser = loadUserByUserId(userId);

        if (group.getGender() != invitedUser.getGender()) {
            throwException(ErrorCode.GENDER_NOT_MATCH, String.format("Gender values of Group(id:%d) and User(id:%d) do not match", groupId, userId));
        }

        if (groupMemberRepository.existsByGroupAndMember(group, invitedUser)) {
            throwException(ErrorCode.DUPLICATED_REQUEST, String.format("User(id: %d) is already a member of Group(id: %d)", userId, groupId));
        }

        GroupInvitation groupInvitation = groupInvitationRepository.findByGroupMember_GroupAndInvitationCode(group, invitationCode).orElseThrow(() ->
                throwException(ErrorCode.INVALID_REQUEST, String.format("InvitationCode(%s) is invalid", invitationCode))
        );

        GroupMember reservedGroupMemberRecord = groupInvitation.getGroupMember();
        reservedGroupMemberRecord.setMember(invitedUser);
        reservedGroupMemberRecord.setStatus(MemberStatus.ACTIVE);
        reservedGroupMemberRecord.setExpiredAt(null);
        GroupMember updated = groupMemberRepository.saveAndFlush(reservedGroupMemberRecord);

        groupInvitationRepository.delete(groupInvitation);
        deleteGroupInvitationQRCodeFromStorage(getGroupInvitationQrImageKey(groupId, groupInvitation.getInvitationCode()));

        return GroupMemberResponse.from(updated);
    }

    @Override
    public void cleanupExpiredInvitations() {
        List<GroupInvitation> expiredInvitations = groupInvitationRepository.findExpiredInvitationsForCleanup(now());

        List<String> keysToDelete = new ArrayList<>();
        List<GroupMember> reservedGroupMemberRecordsToDelete = new ArrayList<>();

        expiredInvitations.forEach(expiredInvitation -> {
            String qrImageKey = getGroupInvitationQrImageKey(expiredInvitation.getGroupMember().getGroup().getId(), expiredInvitation.getInvitationCode());
            keysToDelete.add(qrImageKey);
            reservedGroupMemberRecordsToDelete.add(expiredInvitation.getGroupMember());
        });

        deleteGroupInvitationQRCodesFromStorage(keysToDelete);
        groupInvitationRepository.deleteAll(expiredInvitations);
        groupMemberRepository.deleteAll(reservedGroupMemberRecordsToDelete);
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

    private static String getGroupInvitationQrImageKey(long groupId, String imageFileName) {
        return String.format("%s/%d/%s", QR_CODE_PREFIX, groupId, imageFileName + IMAGE_EXTENSION);
    }

    private String uploadGroupInvitationQRCodeToStorage(long groupId, String imageFileName, byte[] qrImageBytes) {
        return s3StorageManager.uploadByteArrayToS3WithKey(qrImageBytes, getGroupInvitationQrImageKey(groupId, imageFileName));
    }

    private void deleteGroupInvitationQRCodeFromStorage(String keyToDelete) {
        try {
            s3StorageManager.deleteImageByKey(keyToDelete);
        } catch (AmazonS3Exception e) {
            // TODO : aws s3 버킷 에러 세분화
            throwException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void deleteGroupInvitationQRCodesFromStorage(List<String> keysToDelete) {
        try {
            s3StorageManager.deleteImagesByKeys(keysToDelete);
        } catch (AmazonS3Exception e) {
            // TODO : aws s3 버킷 에러 세분화
            throwException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void throwIfUserIsNotTheLeaderOfGroup(User leader, Group group) {
        if (!groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(group, leader, MemberStatus.ACTIVE, MemberRole.LEADER)) {
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
