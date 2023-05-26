package com.ting.ting.service;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupInvitation;
import com.ting.ting.domain.GroupMember;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.MemberStatus;
import com.ting.ting.dto.response.GroupMemberResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.TingApplicationException;
import com.ting.ting.fixture.GroupFixture;
import com.ting.ting.fixture.UserFixture;
import com.ting.ting.repository.GroupInvitationRepository;
import com.ting.ting.repository.GroupMemberRepository;
import com.ting.ting.repository.GroupRepository;
import com.ting.ting.repository.UserRepository;
import com.ting.ting.util.S3StorageManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@DisplayName("[과팅 멤버 초대] 비즈니스 로직 테스트")
@ExtendWith(MockitoExtension.class)
class GroupInvitationServiceTest {

    @InjectMocks
    private GroupInvitationServiceImpl groupInvitationService;

    @Mock private UserRepository userRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private GroupInvitationRepository groupInvitationRepository;
    @Mock private S3StorageManager s3StorageManager;

    private User user;

    @BeforeEach
    private void setUpUser() {
        user = UserFixture.createUserById(1L);
    }

    @DisplayName("[팀장] : 했던 팀 초대들 조회 기능 테스트")
    @Test
    void Given_Group_When_GetGroupMemberInvitationList_Then_ReturnsGroupInvitationResponseSet() {
        //Given
        Long groupId = 1L;

        given(groupRepository.findById(any())).willReturn(Optional.of(mock(Group.class)));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(true);
        given(groupInvitationRepository.findByGroupMember_Group(any())).willReturn(List.of(mock(GroupInvitation.class), mock(GroupInvitation.class)));

        //When & Then
        assertThat(groupInvitationService.findAllGroupMemberInvitation(groupId, user.getId())).hasSize(2);
    }

    @DisplayName("[팀장] : 과팅 멤버 초대 기능 테스트")
    @Test
    void Given_Group_When_CreateGroupMemberInvitation_Then_ReturnsGroupInvitationResponse() {
        //Given
        Long groupId = 1L;

        Group group = GroupFixture.createGroupById(groupId);
        group.setMemberSizeLimit(2);

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(true);
        given(groupMemberRepository.countByGroup(group)).willReturn(1L);
        given(s3StorageManager.uploadByteArrayToS3WithKey(any(), any())).willReturn("qrImagePath");
        given(groupInvitationRepository.save(any())).willReturn(GroupInvitation.of(mock(GroupMember.class), "invitationCode", "qrImageURl", now()));

        //When
        groupInvitationService.createGroupMemberInvitation(groupId, user.getId());

        //Then
        then(s3StorageManager).should().uploadByteArrayToS3WithKey(any(), any());
        then(groupInvitationRepository).should().save(any(GroupInvitation.class));
    }

    @DisplayName("[팀장] : 과팅 멤버 초대 기능 테스트 - 멤버 수가 꽉 찼을 때")
    @Test
    void Given_GroupWhoseMemberCapacityLimitReached_When_CreateGroupMemberInvitation_Then_ThrowsException() {
        //Given
        Long groupId = 1L;

        Group group = GroupFixture.createGroupById(groupId);
        group.setMemberSizeLimit(2);

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(true);
        given(groupMemberRepository.countByGroup(group)).willReturn(2L);

        //When
        Throwable t = catchThrowable(() -> groupInvitationService.createGroupMemberInvitation(groupId, user.getId()));

        //Then
        Assertions.assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REACHED_MEMBERS_SIZE_LIMIT);
        then(groupMemberRepository).shouldHaveNoMoreInteractions();
        then(groupInvitationRepository).shouldHaveNoInteractions();
    }

    @DisplayName("[팀장] : 과팅 멤버 초대 취소 기능 테스트 - 초대 레코드가 DB에 있는 경우")
    @Test
    void Given_GroupAndGroupInvitation_When_DeleteGroupMemberInvitation_Then_DeletesGroupInvitationRecordAndQrImage() {
        //Given
        Long groupId = 1L;
        Long groupInvitationId = 1L;

        Group group = GroupFixture.createGroupById(groupId);
        GroupMember groupMember = GroupMember.of(group, UserFixture.createUserById(2L), MemberStatus.PENDING, MemberRole.MEMBER);
        GroupInvitation groupInvitation = GroupInvitation.of(groupMember, "invitationCode",  "invitationQrImageUrl", now().plus(Duration.ofDays(1)));

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(true);
        given(groupInvitationRepository.findById(groupInvitationId)).willReturn(Optional.of(groupInvitation));

        //When
        groupInvitationService.deleteGroupMemberInvitation(groupId, user.getId(), groupInvitationId);

        //Then
        then(groupInvitationRepository).should().delete(any(GroupInvitation.class));
        then(s3StorageManager).should().deleteImageByKey(any());
    }

    @DisplayName("[팀장] : 과팅 멤버 초대 취소 기능 테스트 - 초대 레코드가 DB에 없는 경우")
    @Test
    void Given_GroupAndGroupInvitationWhichDoesNotExist_When_DeleteGroupMemberInvitation_Then_ThrowsException() {
        //Given
        Long groupId = 1L;
        Long groupInvitationId = 1L;

        given(groupRepository.findById(any())).willReturn(Optional.of(mock(Group.class)));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(true);
        given(groupInvitationRepository.findById(groupInvitationId)).willReturn(Optional.empty());

        //When
        Throwable t = catchThrowable(() -> groupInvitationService.deleteGroupMemberInvitation(groupId, user.getId(), groupInvitationId));

        //Then
        Assertions.assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUEST_NOT_FOUND);
        then(groupMemberRepository).shouldHaveNoMoreInteractions();
        then(groupInvitationRepository).shouldHaveNoMoreInteractions();
        then(s3StorageManager).shouldHaveNoInteractions();
    }

    @DisplayName("[팀장] : 과팅 멤버 초대 취소 기능 테스트 - 그룹의 것이 아닌 초대에 대한 삭제 요청을 보냈을 때")
    @Test
    void Given_GroupAndGroupInvitationWhichDoesNotBelongToTheGroup_When_DeleteGroupMemberInvitation_Then_ThrowsException() {
        //Given
        Long groupId = 1L;
        Long groupInvitationId = 1L;

        Group group = GroupFixture.createGroupById(groupId);
        GroupMember groupMember = GroupMember.of(GroupFixture.createGroupById(2L), UserFixture.createUserById(2L), MemberStatus.PENDING, MemberRole.MEMBER);
        GroupInvitation groupInvitation = GroupInvitation.of(groupMember, "invitationCode",  "invitationQrImageUrl", now());

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(true);
        given(groupInvitationRepository.findById(groupInvitationId)).willReturn(Optional.of(groupInvitation));

        //When
        Throwable t = catchThrowable(() -> groupInvitationService.deleteGroupMemberInvitation(groupId, user.getId(), groupInvitationId));

        //Then
        Assertions.assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
        then(groupInvitationRepository).shouldHaveNoMoreInteractions();
        then(s3StorageManager).shouldHaveNoInteractions();
    }

    @DisplayName("과팅 초대 수락 기능 테스트")
    @Test
    void Given_GroupAndInvitationCode_When_AcceptGroupMemberInvitation_Then_ReturnsCreatedGroupMemberResponse() {
        //Given
        Long groupId = 1L;
        String invitationCode = "invitationCode";

        ReflectionTestUtils.setField(user, "gender", Gender.WOMEN);
        Group group = GroupFixture.createGroupById(groupId);
        ReflectionTestUtils.setField(group, "gender", Gender.WOMEN);
        GroupMember groupMember = GroupMember.of(group, null, MemberStatus.PENDING, MemberRole.MEMBER);
        GroupInvitation groupInvitation = GroupInvitation.of(groupMember, invitationCode,  "invitationQrImageUrl", now());

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupInvitationRepository.findByGroupMember_GroupAndInvitationCode(any(), any())).willReturn(Optional.of(groupInvitation));
        given(groupMemberRepository.saveAndFlush(any())).willReturn(groupInvitation.getGroupMember());

        //When
        GroupMemberResponse updatedResponse = groupInvitationService.acceptGroupMemberInvitation(groupId, user.getId(), invitationCode);

        //Then
        Assertions.assertThat(updatedResponse.getMember().getId()).isSameAs(user.getId());
        Assertions.assertThat(updatedResponse.getStatus()).isSameAs(MemberStatus.ACTIVE);
        then(groupInvitationRepository).should().delete(any(GroupInvitation.class));
        then(s3StorageManager).should().deleteImageByKey(any());
    }

    @DisplayName("과팅 초대 수락 기능 테스트 - 팀과 성별이 다른 user 가 초대 요청 수락을 했을 때")
    @Test
    void Given_GroupWhoseGenderDoesNotMatchTheUserAndInvitationCode_When_AcceptGroupMemberInvitation_Then_ThrowsException() {
        //Given
        Long groupId = 1L;
        String invitationCode = "invitationCode";

        ReflectionTestUtils.setField(user, "gender", Gender.MEN);
        Group group = GroupFixture.createGroupById(groupId);
        ReflectionTestUtils.setField(group, "gender", Gender.WOMEN);

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));

        //When
        Throwable t = catchThrowable(() -> groupInvitationService.acceptGroupMemberInvitation(groupId, user.getId(), invitationCode));

        //Then
        Assertions.assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GENDER_NOT_MATCH);
        then(groupMemberRepository).shouldHaveNoInteractions();
        then(groupInvitationRepository).shouldHaveNoInteractions();
        then(s3StorageManager).shouldHaveNoInteractions();
    }

    @DisplayName("과팅 초대 수락 기능 테스트 - invalid 한 invitationCode 로 접근했을 때")
    @Test
    void Given_GroupAndInvitationCodeWhichIsInvalid_When_AcceptGroupMemberInvitation_Then_ThrowsException() {
        //Given
        Long groupId = 1L;
        String invitationCode = "invitationCode";

        ReflectionTestUtils.setField(user, "gender", Gender.WOMEN);
        Group group = GroupFixture.createGroupById(groupId);
        ReflectionTestUtils.setField(group, "gender", Gender.WOMEN);

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.existsByGroupAndMember(any(), any())).willReturn(false);
        given(groupInvitationRepository.findByGroupMember_GroupAndInvitationCode(any(), any())).willReturn(Optional.empty());

        //When
        Throwable t = catchThrowable(() -> groupInvitationService.acceptGroupMemberInvitation(groupId, user.getId(), invitationCode));

        //Then
        Assertions.assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
        then(groupMemberRepository).shouldHaveNoMoreInteractions();
        then(groupInvitationRepository).shouldHaveNoMoreInteractions();
        then(s3StorageManager).shouldHaveNoInteractions();
    }

    @DisplayName("만료된 초대 코드 삭제")
    @Test
    void Given_Nothing_When_CleanupExpiredInvitations_Then_DeletesExpiredGroupInvitationsAndGroupMembers() {
        //Given
        given(groupInvitationRepository.findExpiredInvitationsForCleanup(any())).willReturn(List.of());

        //When
        groupInvitationService.cleanupExpiredInvitations();

        //Then
        then(s3StorageManager).should().deleteImagesByKeys(any());
        then(groupInvitationRepository).should().deleteAll(any());
        then(groupMemberRepository).should().deleteAll(any());
    }
}