package com.ting.ting.service;

import com.ting.ting.domain.*;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.MemberStatus;
import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.GroupDateRequestResponse;
import com.ting.ting.dto.response.GroupDateRequestWithFromAndToResponse;
import com.ting.ting.dto.response.GroupMemberResponse;
import com.ting.ting.dto.response.GroupResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.TingApplicationException;
import com.ting.ting.fixture.GroupFixture;
import com.ting.ting.fixture.UserFixture;
import com.ting.ting.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("[과팅] 비즈니스 로직 테스트")
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @InjectMocks private GroupServiceImpl groupService;

    @Mock private GroupRepository groupRepository;
    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private GroupMemberRequestRepository groupMemberRequestRepository;
    @Mock private GroupDateRepository groupDateRepository;
    @Mock private GroupDateRequestRepository groupDateRequestRepository;
    @Mock private UserRepository userRepository;

    private User user;

    @BeforeEach
    private void setUpUser() {
        user = UserFixture.createUserById(1L);
    }

    @DisplayName("모든 팀 조회 성공")
    @Test
    void Given_Nothing_When_FindAllGroups_thenReturnsGroupResponsePage() {
        //Given
        Pageable pageable = Pageable.ofSize(20);
        given(groupRepository.findAll(pageable)).willReturn(Page.empty());

        //When & Then
        assertThat(groupService.findAllGroups(pageable)).isEmpty();
    }

    @DisplayName("같은 성별 팀 가입을 위한 조회 기능 테스트")
    @Test
    void Given_Nothing_When_FindSuggestedSameGenderGroupList_ThenReturnsGroupWithRequestStatusResponsePage() {
        //Given
        Pageable pageable = Pageable.ofSize(20);

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupRepository.findAllSuggestedGroupWithRequestStatusByUserAndGender(any(), any(), any())).willReturn(Page.empty());

        //When
        assertThat(groupService.findSuggestedSameGenderGroupList(user.getId(), pageable)).isEmpty();
    }

    @DisplayName("내가 속한 팀 조회 기능 테스트")
    @Test
    void Given_Nothing_When_FindMyGroupList_ThenReturnsGroupSet() {
        //Given
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.findAllGroupByMemberAndStatus(any(), any())).willReturn(List.of(mock(Group.class), mock(Group.class)));

        //When & Then
        assertThat(groupService.findMyGroupList(user.getId())).hasSize(2);
    }

    @DisplayName("팀 멤버 조회 기능 테스트")
    @Test
    void Given_Group_When_FindGroupMemberList_Then_ReturnsGroupMemberSet() {
        //Given
        Long groupId = 1L;

        Group group = GroupFixture.createGroupById(groupId);
        GroupMember member1 = GroupMember.of(group, user, MemberStatus.PENDING, MemberRole.MEMBER);
        GroupMember member2 = GroupMember.of(group, UserFixture.createUserById(user.getId() + 1), MemberStatus.ACTIVE, MemberRole.MEMBER);

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(groupMemberRepository.findAllByGroup(any())).willReturn(List.of(member1, member2));

        //When & Then
        assertThat(groupService.findGroupMemberList(group.getId())).hasSize(2);
    }

    @DisplayName("팀 생성 기능 테스트")
    @Test
    void Given_GroupRequest_When_SaveGroup_Then_ReturnsCreatedGroup() {
        //Given
        GroupRequest request = GroupFixture.request();

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupRepository.findByGroupName(request.getGroupName())).willReturn(Optional.empty());
        given(groupRepository.save(any())).willReturn(request.toEntity());

        //When
        GroupResponse actual = groupService.saveGroup(user.getId(), request);

        //Then
        assertThat(actual.getGroupName()).isSameAs(request.getGroupName());
        then(groupMemberRepository).should().save(any(GroupMember.class));
    }

    @DisplayName("멤버 가입 요청 기능 테스트")
    @Test
    void Given_Group_When_SaveJoinRequest_Then_SavesRequest() {
        //Given
        Long groupId = 1L;

        ReflectionTestUtils.setField(user, "gender", Gender.WOMEN);
        Group group = GroupFixture.createGroupById(groupId);
        ReflectionTestUtils.setField(group, "gender", Gender.WOMEN);
        ReflectionTestUtils.setField(group, "isJoinable", true);

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRequestRepository.findByGroupAndUser(any(), any())).willReturn(Optional.empty());
        given(groupMemberRepository.existsByGroupAndMember(any(), any())).willReturn(false);
        given(groupMemberRequestRepository.save(any())).willReturn(any(GroupMemberRequest.class));

        //When & Then
        assertDoesNotThrow(() -> groupService.saveJoinRequest(groupId, user.getId()));
    }

    @DisplayName("멤버 가입 요청 기능 테스트 - 성별이 다른 경우")
    @Test
    void Given_GroupAndUserWithDifferentGenderValues_When_SaveJoinRequest_Then_ThrowsException() {
        //Given
        Long groupId = 1L;

        Group group = GroupFixture.createGroupById(groupId);
        ReflectionTestUtils.setField(group, "gender", Gender.MEN);
        ReflectionTestUtils.setField(user, "gender", Gender.WOMEN);

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));

        //When
        Throwable t = catchThrowable(() -> groupService.saveJoinRequest(groupId, user.getId()));

        //Then
        assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GENDER_NOT_MATCH);
        then(groupMemberRequestRepository).shouldHaveNoInteractions();
    }

    @DisplayName("멤버 가입 요청 기능 테스트 - 이미 팀에 가입 요청을 보낸 멤버일 경우")
    @Test
    void Given_GroupAndUserWhoAlreadyRequestedToJoin_When_SaveJoinRequest_Then_ThrowsException() {
        //Given
        Long groupId = 1L;

        Group group = GroupFixture.createGroupById(groupId);
        ReflectionTestUtils.setField(group, "isJoinable", true);

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRequestRepository.findByGroupAndUser(any(), any())).willReturn(Optional.of(mock(GroupMemberRequest.class)));

        //When
        Throwable t = catchThrowable(() -> groupService.saveJoinRequest(groupId, user.getId()));

        //Then
        assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATED_REQUEST);
        then(groupMemberRequestRepository).shouldHaveNoMoreInteractions();
    }

    @DisplayName("멤버 가입 요청 기능 테스트 - 이미 팀에 소속된 멤버일 경우")
    @Test
    void Given_GroupAndUserWhoIsAMemberOfTheGroup_When_SaveJoinRequest_Then_ThrowsException() {
        //Given
        Long groupId = 1L;

        Group group = GroupFixture.createGroupById(groupId);
        ReflectionTestUtils.setField(group, "isJoinable", true);

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRequestRepository.findByGroupAndUser(any(), any())).willReturn(Optional.empty());
        given(groupMemberRepository.existsByGroupAndMember(any(), any())).willReturn(true);

        //When
        Throwable t = catchThrowable(() -> groupService.saveJoinRequest(groupId, user.getId()));

        //Then
        assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_JOINED);
        then(groupMemberRequestRepository).shouldHaveNoMoreInteractions();
    }

    @DisplayName("멤버 가입 요청 취소 기능 테스트")
    @Test
    void Given_Group_When_DeleteJoinRequest_Then_DeletesRequest() {
        //Given
        Long groupId = 1L;

        willDoNothing().given(groupMemberRequestRepository).deleteByGroup_IdAndUser_Id(any(), any());

        //When & Then
        assertDoesNotThrow(() -> groupService.deleteJoinRequest(groupId, user.getId()));
    }

    @DisplayName("팀 나오기 기능 테스트 - 나오려는 유저가 팀의 리더가 아닌 경우")
    @Test
    void Given_Group_When_DeleteGroupMember_Then_DeletesMember() {
        //Given
        Long groupId = 1L;

        Group group = GroupFixture.createGroupById(groupId);
        GroupMember memberRecordOfMember = GroupMember.of(group, user, MemberStatus.ACTIVE, MemberRole.MEMBER);

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.findByGroupAndMemberAndStatus(any(), any(), any())).willReturn(Optional.of(memberRecordOfMember));

        //When
        groupService.deleteGroupMember(groupId, user.getId());

        //Then
        assertDoesNotThrow(() -> groupMemberRepository.delete(memberRecordOfMember));
    }

    @DisplayName("팀 나오기 기능 테스트 - 나오려는 유저가 팀의 리더인 경우, 팀장 가능한 멤버가 있는 경우")
    @Test
    void Given_GroupWithAvailableMemberAsNewLeaderAndLeaderWhen_DeleteGroupMember_Then_DeletesLeaderAndMakesTheMemberAsNewLeader() {
        //Given
        Long groupId = 1L;

        Group group = GroupFixture.createGroupById(groupId);
        User newLeader = UserFixture.createUserById(user.getId() + 1);
        GroupMember memberRecordOfLeader = GroupMember.of(group, user, MemberStatus.ACTIVE, MemberRole.LEADER);
        GroupMember memberRecordOfMember = GroupMember.of(group, newLeader, MemberStatus.ACTIVE, MemberRole.MEMBER);

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.findByGroupAndMemberAndStatus(any(), any(), any())).willReturn(Optional.of(memberRecordOfLeader));
        given(groupMemberRepository.findAvailableMemberAsALeaderInGroup(any(), any())).willReturn(List.of(memberRecordOfMember));

        //When
        groupService.deleteGroupMember(groupId, user.getId());

        //Then
        assertThat(memberRecordOfMember.getRole()).isSameAs(MemberRole.LEADER);
        then(groupMemberRepository).should().delete(any());
        then(groupMemberRepository).shouldHaveNoMoreInteractions();
    }

    @DisplayName("팀 나오기 기능 테스트 - 나오려는 유저가 팀의 리더인 경우, 팀장 가능한 멤버가 없는 경우")
    @Test
    void Given_GroupWithNoAvailableMemberAsNewLeaderAndLeader_When_DeleteGroupMember_Then_ThrowsException() {
        //Given
        Long groupId = 1L;

        GroupMember memberRecordOfLeader = GroupMember.of(GroupFixture.createGroupById(groupId), user, MemberStatus.ACTIVE, MemberRole.LEADER);

        given(groupRepository.findById(any())).willReturn(Optional.of(mock(Group.class)));
        given(userRepository.findById(any())).willReturn(Optional.of(mock(User.class)));
        given(groupMemberRepository.findByGroupAndMemberAndStatus(any(), any(), any())).willReturn(Optional.of(memberRecordOfLeader));
        given(groupMemberRepository.findAvailableMemberAsALeaderInGroup(any(), any())).willReturn(List.of());

        //When
        Throwable t = catchThrowable(() ->  groupService.deleteGroupMember(groupId, user.getId()));

        //Then
        assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AVAILABLE_MEMBER_AS_LEADER);
        then(groupMemberRepository).shouldHaveNoMoreInteractions();
    }

    @DisplayName("[팀장] : 팀장 넘기기 기능 테스트")
    @Test
    void Given_GroupAndMemberAsNewLeader_When_ChangeGroupLeader_Then_ReturnsGroupMemberResponseOfNewLeader() {
        //Given
        Long groupId = 1L;

        Group group = GroupFixture.createGroupById(1L);
        User newLeader = UserFixture.createUserById(user.getId() + 1);
        GroupMember memberRecordOfLeader = GroupMember.of(group, user, MemberStatus.ACTIVE, MemberRole.LEADER);
        GroupMember memberRecordOfMember = GroupMember.of(group, newLeader, MemberStatus.ACTIVE, MemberRole.MEMBER);

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(userRepository.findById(any())).willReturn(Optional.of(newLeader));
        given(groupMemberRepository.existsByMemberAndStatusAndRole(any(), any(), any())).willReturn(false);
        given(groupMemberRepository.findByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(Optional.of(memberRecordOfLeader)).willReturn(Optional.of(memberRecordOfMember));

        //When
        Set<GroupMemberResponse> actual = groupService.changeGroupLeader(groupId, user.getId(), newLeader.getId());

        //Then
        assertThat(memberRecordOfLeader.getRole()).isSameAs(MemberRole.MEMBER);
        assertThat(memberRecordOfMember.getRole()).isSameAs(MemberRole.LEADER);
    }

    @DisplayName("[팀장] : 멤버 가입 요청 조회 기능 테스트")
    @Test
    void Given_Group_When_FindMemberJoinRequest_Then_ReturnsGroupMemberRequestSet() {
        //Given
        Long groupId = 1L;

        Group group = GroupFixture.createGroupById(groupId);
        GroupMemberRequest request1 = GroupMemberRequest.of(group, UserFixture.createUserById(user.getId() + 1));
        GroupMemberRequest request2 = GroupMemberRequest.of(group, UserFixture.createUserById(user.getId() + 1));

        given(groupRepository.findById(any())).willReturn(Optional.of(mock(Group.class)));
        given(userRepository.findById(any())).willReturn(Optional.of(mock(User.class)));
        given(groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(true);
        given(groupMemberRequestRepository.findByGroup(any())).willReturn(List.of(request1, request2));

        //When & Then
        assertThat(groupService.findMemberJoinRequest(groupId, user.getId())).hasSize(2);
    }

    @DisplayName("[팀장] : 멤버 가입 요청 수락 기능 테스트")
    @Test
    void Given_GroupMemberRequest_When_AcceptMemberJoinRequest_Then_ReturnsCreatedGroupMemberResponse() {
        //Given
        Long groupMemberRequestId = 1L;

        Group group = GroupFixture.createGroupById(1L);
        group.setNumOfMember(3);
        GroupMemberRequest request = GroupMemberRequest.of(group, UserFixture.createUserById(user.getId() + 1));

        given(userRepository.findById(any())).willReturn(Optional.of(mock(User.class)));
        given(groupMemberRequestRepository.findById(any())).willReturn(Optional.of(request));
        given(groupMemberRepository.existsByGroupAndMember(any(), any())).willReturn(false);
        given(groupMemberRepository.countByGroup(group)).willReturn(2L);
        given(groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(true);
        given(groupMemberRepository.save(any())).willReturn(GroupMember.of(group, request.getUser(), MemberStatus.ACTIVE, MemberRole.MEMBER));

        //When
        GroupMemberResponse actual = groupService.acceptMemberJoinRequest(user.getId(), groupMemberRequestId);

        //Then
        assertThat(actual.getMember().getUsername()).isSameAs(request.getUser().getUsername());
        then(groupMemberRepository).should().save(any(GroupMember.class));
        then(groupMemberRequestRepository).should().delete(any());
    }

    @DisplayName("[팀장] : 멤버 가입 요청 수락 기능 테스트 - 요청한 유저가 이미 멤버인 경우")
    @Test
    void Given_GroupMemberRequestWhichContainsMember_When_AcceptMemberJoinRequest_Then_ThrowsException() {
        //Given
        Long groupMemberRequestId = 1L;

        GroupMemberRequest request = GroupMemberRequest.of(GroupFixture.createGroupById(1L), UserFixture.createUserById(user.getId() + 1));

        given(userRepository.findById(any())).willReturn(Optional.of(mock(User.class)));
        given(groupMemberRequestRepository.findById(any())).willReturn(Optional.of(request));
        given(groupMemberRepository.existsByGroupAndMember(any(), any())).willReturn(true);

        //When
        Throwable t = catchThrowable(() -> groupService.acceptMemberJoinRequest(user.getId(), groupMemberRequestId));

        //Then
        assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATED_REQUEST);
        then(groupMemberRequestRepository).should().delete(any(GroupMemberRequest.class));
        then(groupMemberRepository).shouldHaveNoMoreInteractions();
    }

    @DisplayName("[팀장] : 멤버 가입 요청 수락 기능 테스트 - 팀의 가능한 멤버 수가 꽉찬 경우")
    @Test
    void Given_GroupMemberRequestWhichContainsFullGroup_When_AcceptMemberJoinRequest_Then_ThrowsException() {
        //Given
        Long groupMemberRequestId = 1L;

        Group group = GroupFixture.createGroupById(1L);
        group.setNumOfMember(3);
        GroupMemberRequest request = GroupMemberRequest.of(group, UserFixture.createUserById(user.getId() + 1));

        given(userRepository.findById(any())).willReturn(Optional.of(mock(User.class)));
        given(groupMemberRequestRepository.findById(any())).willReturn(Optional.of(request));
        given(groupMemberRepository.existsByGroupAndMember(any(), any())).willReturn(false);
        given(groupMemberRepository.countByGroup(group)).willReturn(3L);

        //When
        Throwable t = catchThrowable(() -> groupService.acceptMemberJoinRequest(user.getId(), groupMemberRequestId));

        //Then
        assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REACHED_MEMBERS_SIZE_LIMIT);
        then(groupMemberRequestRepository).shouldHaveNoMoreInteractions();
        then(groupMemberRepository).shouldHaveNoMoreInteractions();
    }

    @DisplayName("[팀장] : 멤버 가입 요청 거절 기능 테스트")
    @Test
    void Given_GroupMemberRequest_When_RejectMemberJoinRequest_Then_DeletesMemberJoinRequest() {
        //Given
        Long groupMemberRequestId = 1L;

        given(userRepository.findById(any())).willReturn(Optional.of(mock(User.class)));
        given(groupMemberRequestRepository.findById(any())).willReturn(Optional.of(mock(GroupMemberRequest.class)));
        given(groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(true);

        //When
        groupService.rejectMemberJoinRequest(user.getId(), groupMemberRequestId);

        //Then
        then(groupMemberRequestRepository).should().delete(any(GroupMemberRequest.class));
    }

    @DisplayName("[팀장] : 과팅 요청 조회 기능 테스트")
    @Test
    void Given_Group_When_FindAllGroupDateRequest_Then_ReturnsGroupDateRequestResponseSet() {
        //Given
        Long groupId = 1L;

        Group group = GroupFixture.createGroupById(groupId);

        given(groupRepository.findById(any())).willReturn(Optional.of(mock(Group.class)));
        given(userRepository.findById(any())).willReturn(Optional.of(mock(User.class)));
        given(groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(true);
        given(groupDateRequestRepository.findToGroupByFromGroup(any())).willReturn(List.of(GroupFixture.createGroupById(4L), GroupFixture.createGroupById(5L), GroupFixture.createGroupById(6L)));
        given(groupDateRequestRepository.findFromGroupByToGroup(any())).willReturn(List.of(GroupFixture.createGroupById(2L), GroupFixture.createGroupById(3L)));

        //When
        GroupDateRequestWithFromAndToResponse created = groupService.findAllGroupDateRequest(groupId, user.getId());

        //Then
        assertThat(created.getSentGroupDateRequests()).hasSize(3);
        assertThat(created.getReceivedGroupDateRequests()).hasSize(2);
    }

    @DisplayName("[팀장] : 과팅 요청 기능 테스트")
    @Test
    void Given_FromGroupAndToGroup_When_SaveGroupDateRequest_Then_ReturnsCreatedGroupDateRequestResponse() {
        //Given
        Long fromGroupId = 1L;
        Long toGroupId = 2L;

        ReflectionTestUtils.setField(user, "gender", Gender.WOMEN);
        Group fromGroup = GroupFixture.createGroupById(fromGroupId);
        ReflectionTestUtils.setField(fromGroup, "gender", Gender.WOMEN);
        Group toGroup = GroupFixture.createGroupById(toGroupId);
        ReflectionTestUtils.setField(toGroup, "gender", Gender.MEN);

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupRepository.findById(any())).willReturn(Optional.of(fromGroup)).willReturn(Optional.of(toGroup));
        given(groupDateRequestRepository.existsByFromGroupAndToGroup(any(), any())).willReturn(false);
        given(groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(true);
        given(groupDateRequestRepository.existsByFromGroupAndToGroup(any(), any())).willReturn(false);
        given(groupDateRequestRepository.save(any())).willReturn(GroupDateRequest.of(fromGroup, toGroup));

        //When
        GroupDateRequestResponse created = groupService.saveGroupDateRequest(user.getId(), fromGroupId, toGroupId);

        //Then
        assertThat(created.getFromGroup().getId()).isSameAs(fromGroup.getId());
        assertThat(created.getToGroup().getId()).isSameAs(toGroup.getId());
    }

    @DisplayName("[팀장] : 과팅 요청 기능 테스트 - 같은 성별인 팀에 요청을 보낼 경우")
    @Test
    void Given_FromGroupAndToGroupWhoseGendersAreTheSame_When_SaveGroupDateRequest_Then_ThrowsException() {
        //Given
        Long fromGroupId = 1L;
        Long toGroupId = 2L;

        Group fromGroup = GroupFixture.createGroupById(fromGroupId);
        ReflectionTestUtils.setField(fromGroup, "gender", Gender.WOMEN);
        Group toGroup = GroupFixture.createGroupById(toGroupId);
        ReflectionTestUtils.setField(toGroup, "gender", Gender.WOMEN);

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupRepository.findById(any())).willReturn(Optional.of(fromGroup)).willReturn(Optional.of(toGroup));

        //When
        Throwable t = catchThrowable(() -> groupService.saveGroupDateRequest(user.getId(), fromGroupId, toGroupId));

        //Then
        assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
    }

    @DisplayName("[팀장] : 과팅 요청 취소 기능 테스트")
    @Test
    void Given_FromGroupAndToGroup_When_DeleteGroupDateRequest_Then_DeletesGroupDateRequest() {
        //Given
        Long fromGroupId = 1L;
        Long toGroupId = 2L;

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupRepository.findById(any())).willReturn(Optional.of(mock(Group.class)));
        given(groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(true);

        //When
        groupService.deleteGroupDateRequest(user.getId(), fromGroupId, toGroupId);

        //Then
        then(groupDateRequestRepository).should().deleteByFromGroup_IdAndToGroup_Id(any(), any());
    }

    @DisplayName("[팀장] : 과팅 요청 수락 기능 테스트")
    @Test
    void Given_GroupDateRequest_When_AcceptGroupDateRequest_Then_ReturnsCreatedGroupDateResponse() {
        //Given
        Long groupDateRequestId = 1L;

        Group fromGroup = GroupFixture.createGroupById(1L);
        ReflectionTestUtils.setField(fromGroup, "gender", Gender.WOMEN);
        Group toGroup = GroupFixture.createGroupById(2L);
        ReflectionTestUtils.setField(toGroup, "gender", Gender.MEN);
        GroupDateRequest groupDateRequest = GroupDateRequest.of(fromGroup, toGroup);
        ReflectionTestUtils.setField(user, "gender", Gender.MEN);

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupDateRequestRepository.findById(any())).willReturn(Optional.of(groupDateRequest));
        given(groupDateRepository.existsByMenGroupOrWomenGroup(any(), any())).willReturn(false);
        given(groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(true);
        given(groupDateRepository.save(any())).willReturn(GroupDate.of(toGroup, fromGroup));

        //When
        groupService.acceptGroupDateRequest(user.getId(), groupDateRequestId);

        //Then
        assertThat(toGroup.isMatched()).isSameAs(true);
        assertThat(fromGroup.isMatched()).isSameAs(true);
        then(groupDateRepository).should().save(any(GroupDate.class));
        then(groupDateRequestRepository).should().delete(any(GroupDateRequest.class));
        then(groupDateRequestRepository).should().delete(any(GroupDateRequest.class));
    }

    @DisplayName("[팀장] : 과팅 요청 수락 기능 테스트 - 이미 매칭된 과팅이 있는 경우")
    @Test
    void Given_GroupDateRequestContainingGroupThatHasMatchedGroupMeeting_When_AcceptGroupDateRequest_Then_ThrowsException() {
        //Given
        Long groupDateRequestId = 1L;

        Group fromGroup = GroupFixture.createGroupById(1L);
        ReflectionTestUtils.setField(fromGroup, "gender", Gender.WOMEN);
        Group toGroup = GroupFixture.createGroupById(2L);
        ReflectionTestUtils.setField(toGroup, "gender", Gender.MEN);
        GroupDateRequest groupDateRequest = GroupDateRequest.of(fromGroup, toGroup);
        ReflectionTestUtils.setField(user, "gender", Gender.MEN);

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupDateRequestRepository.findById(any())).willReturn(Optional.of(groupDateRequest));
        given(groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(true);
        given(groupDateRepository.existsByMenGroupOrWomenGroup(any(), any())).willReturn(true);

        //When
        Throwable t = catchThrowable(() -> groupService.acceptGroupDateRequest(user.getId(), groupDateRequestId));

        //Then
        assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATED_REQUEST);
        then(groupDateRepository).shouldHaveNoMoreInteractions();
    }

    @DisplayName("[팀장] : 과팅 요청 삭제 기능 테스트")
    @Test
    void Given_GroupDateRequest_When_RejectGroupDateRequest_Then_DeletesGroupDateRequest() {
        //Given
        Long groupDateRequestId = 1L;

        given(userRepository.findById(any())).willReturn(Optional.of(mock(User.class)));
        given(groupDateRequestRepository.findById(any())).willReturn(Optional.of(mock(GroupDateRequest.class)));
        given(groupMemberRepository.existsByGroupAndMemberAndStatusAndRole(any(), any(), any(), any())).willReturn(true);

        //When & Then
        assertDoesNotThrow(() -> groupService.rejectGroupDateRequest(user.getId(), groupDateRequestId));
    }
}