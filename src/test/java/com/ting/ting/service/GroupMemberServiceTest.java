package com.ting.ting.service;

import com.ting.ting.domain.*;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.LikeStatus;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.domain.custom.GroupWithMemberCount;
import com.ting.ting.dto.idealPhoto.MixedImageResponse;
import com.ting.ting.dto.response.GroupMemberResponse;
import com.ting.ting.dto.response.JoinableGroupResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.TingApplicationException;
import com.ting.ting.fixture.GroupFixture;
import com.ting.ting.fixture.UserFixture;
import com.ting.ting.repository.*;
import com.ting.ting.util.IdealPhotoManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@DisplayName("[과팅] 팀 멤버 관련 비즈니스 로직 테스트")
@ExtendWith(MockitoExtension.class)
public class GroupMemberServiceTest {

    @InjectMocks private GroupMemberServiceImpl groupMemberService;

    @Mock private UserRepository userRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private GroupLikeToJoinRepository groupLikeToJoinRepository;
    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private GroupMemberRequestRepository groupMemberRequestRepository;
    @Mock private IdealPhotoManager idealPhotoManager;

    private User user;

    @BeforeEach
    private void setUpUser() {
        user = UserFixture.createUserById(1L);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getName()).willReturn(user.getId().toString()); // 원하는 userId 값을 반환하도록 설정
    }

    @DisplayName("팀 멤버 조회 기능 테스트")
    @Disabled
    @Test
    void Given_Group_When_FindGroupMemberList_Then_ReturnsGroupMemberSet() {
        //Given
        Long groupId = 1L;

        Group group = GroupFixture.createGroupById(groupId);
        GroupMember member1 = GroupMember.of(group, user, MemberRole.MEMBER);
        GroupMember member2 = GroupMember.of(group, UserFixture.createUserById(user.getId() + 1), MemberRole.MEMBER);

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(groupMemberRepository.findAllByGroup(any())).willReturn(List.of(member1, member2));

        //When & Then
        assertThat(groupMemberService.findGroupMemberList(group.getId())).hasSize(2);
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
        assertDoesNotThrow(() -> groupMemberService.saveJoinRequest(groupId));
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
        Throwable t = catchThrowable(() -> groupMemberService.saveJoinRequest(groupId));

        //Then
        Assertions.assertThat(t)
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
        Throwable t = catchThrowable(() -> groupMemberService.saveJoinRequest(groupId));

        //Then
        Assertions.assertThat(t)
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
        Throwable t = catchThrowable(() -> groupMemberService.saveJoinRequest(groupId));

        //Then
        Assertions.assertThat(t)
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
        assertDoesNotThrow(() -> groupMemberService.deleteJoinRequest(groupId));
    }

    @DisplayName("팀 나오기 기능 테스트 - 나오려는 유저가 팀의 리더가 아닌 경우")
    @Test
    void Given_Group_When_DeleteGroupMember_Then_DeletesMember() {
        //Given
        Long groupId = 1L;

        Group group = GroupFixture.createGroupById(groupId);
        GroupMember memberRecordOfMember = GroupMember.of(group, user, MemberRole.MEMBER);

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.findByGroupAndMember(any(), any())).willReturn(Optional.of(memberRecordOfMember));

        //When
        groupMemberService.deleteGroupMember(groupId);

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
        GroupMember memberRecordOfLeader = GroupMember.of(group, user, MemberRole.LEADER);
        GroupMember memberRecordOfMember = GroupMember.of(group, newLeader, MemberRole.MEMBER);

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.findByGroupAndMember(any(), any())).willReturn(Optional.of(memberRecordOfLeader));
        given(groupMemberRepository.findAvailableMemberAsALeaderInGroup(any(), any())).willReturn(List.of(memberRecordOfMember));

        //When
        groupMemberService.deleteGroupMember(groupId);

        //Then
        Assertions.assertThat(memberRecordOfMember.getRole()).isSameAs(MemberRole.LEADER);
        then(groupMemberRepository).should().delete(any());
        then(groupMemberRepository).shouldHaveNoMoreInteractions();
    }

    @DisplayName("팀 나오기 기능 테스트 - 나오려는 유저가 팀의 리더인 경우, 팀장 가능한 멤버가 없는 경우")
    @Test
    void Given_GroupWithNoAvailableMemberAsNewLeaderAndLeader_When_DeleteGroupMember_Then_ThrowsException() {
        //Given
        Long groupId = 1L;

        GroupMember memberRecordOfLeader = GroupMember.of(GroupFixture.createGroupById(groupId), user, MemberRole.LEADER);

        given(groupRepository.findById(any())).willReturn(Optional.of(mock(Group.class)));
        given(userRepository.findById(any())).willReturn(Optional.of(mock(User.class)));
        given(groupMemberRepository.findByGroupAndMember(any(), any())).willReturn(Optional.of(memberRecordOfLeader));
        given(groupMemberRepository.findAvailableMemberAsALeaderInGroup(any(), any())).willReturn(List.of());

        //When
        Throwable t = catchThrowable(() ->  groupMemberService.deleteGroupMember(groupId));

        //Then
        Assertions.assertThat(t)
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
        GroupMember memberRecordOfLeader = GroupMember.of(group, user, MemberRole.LEADER);
        GroupMember memberRecordOfMember = GroupMember.of(group, newLeader, MemberRole.MEMBER);

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(userRepository.findById(any())).willReturn(Optional.of(newLeader));
        given(groupMemberRepository.existsByMemberAndRole(any(), any())).willReturn(false);
        given(groupMemberRepository.findByGroupAndMemberAndRole(any(), any(), any())).willReturn(Optional.of(memberRecordOfLeader)).willReturn(Optional.of(memberRecordOfMember));

        //When
        Set<GroupMemberResponse> actual = groupMemberService.changeGroupLeader(groupId, newLeader.getId());

        //Then
        Assertions.assertThat(memberRecordOfLeader.getRole()).isSameAs(MemberRole.MEMBER);
        Assertions.assertThat(memberRecordOfMember.getRole()).isSameAs(MemberRole.LEADER);
    }

    @DisplayName("유저가 한 가입 요청 조회 기능 테스트")
    @Test
    void Given_Nothing_When_FindUserJoinRequestList_Then_ReturnsJoinRequestResponse() {
        //Given
        Pageable pageable = Pageable.ofSize(20);

        Group requestedGroup = GroupFixture.createGroupById(1L);
        GroupWithMemberCount requestedGroupWithMemberCount = new GroupWithMemberCount(requestedGroup.getId(), requestedGroup.getGroupName(), requestedGroup.getGender(), 2L, requestedGroup.getMemberSizeLimit(), requestedGroup.getSchool(), requestedGroup.isMatched(), true, requestedGroup.getMemo(), requestedGroup.getIdealPhoto(), requestedGroup.getCreatedAt());
        GroupMemberRequest requests = GroupMemberRequest.of(requestedGroup, user);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(groupMemberRequestRepository.findAllByUser(user, pageable)).willReturn(new PageImpl<>(List.of(requests)));
        given(groupLikeToJoinRepository.findAllByFromUser(user)).willReturn(List.of(GroupLikeToJoin.of(user, requestedGroup)));
        given(groupRepository.findAllWithMemberCountByIdIn(any())).willReturn(List.of(requestedGroupWithMemberCount));

        //When
        Page<JoinableGroupResponse> created = groupMemberService.findUserJoinRequestList(pageable);

        //Then
        List<JoinableGroupResponse> createdList = created.getContent().stream().collect(Collectors.toList());
        Assertions.assertThat(createdList).hasSize(1);
        Assertions.assertThat(createdList.get(0)).hasFieldOrPropertyWithValue("likeStatus", LikeStatus.LIKED);
        Assertions.assertThat(createdList.get(0)).hasFieldOrPropertyWithValue("requestStatus", RequestStatus.PENDING);
        Assertions.assertThat(createdList.get(0).getGroup().getMemberCount()).isSameAs(2);
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
        given(groupMemberRepository.existsByGroupAndMemberAndRole(any(), any(), any())).willReturn(true);
        given(groupMemberRequestRepository.findByGroup(any())).willReturn(List.of(request1, request2));

        //When & Then
        assertThat(groupMemberService.findMemberJoinRequest(groupId)).hasSize(2);
    }

    @DisplayName("[팀장] : 멤버 가입 요청 수락 기능 테스트")
    @Test
    void Given_GroupMemberRequest_When_AcceptMemberJoinRequest_Then_ReturnsCreatedGroupMemberResponse() {
        //Given
        Long groupMemberRequestId = 1L;

        Group group = GroupFixture.createGroupById(1L);
        group.setMemberSizeLimit(3);
        GroupMemberRequest request = GroupMemberRequest.of(group, UserFixture.createUserById(user.getId() + 1));

        given(userRepository.findById(any())).willReturn(Optional.of(mock(User.class)));
        given(groupMemberRequestRepository.findById(any())).willReturn(Optional.of(request));
        given(groupMemberRepository.existsByGroupAndMember(any(), any())).willReturn(false);
        given(groupMemberRepository.countByGroup(group)).willReturn(2L);
        given(groupMemberRepository.existsByGroupAndMemberAndRole(any(), any(), any())).willReturn(true);
        given(groupMemberRepository.save(any())).willReturn(GroupMember.of(group, request.getUser(), MemberRole.MEMBER));
        given(idealPhotoManager.mixIdealPhotos(any(), any())).willReturn(mock(MixedImageResponse.class));

        //When
        GroupMemberResponse actual = groupMemberService.acceptMemberJoinRequest(groupMemberRequestId);

        //Then
        Assertions.assertThat(actual.getMember().getUsername()).isSameAs(request.getUser().getUsername());
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
        Throwable t = catchThrowable(() -> groupMemberService.acceptMemberJoinRequest(groupMemberRequestId));

        //Then
        Assertions.assertThat(t)
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
        group.setMemberSizeLimit(3);
        GroupMemberRequest request = GroupMemberRequest.of(group, UserFixture.createUserById(user.getId() + 1));

        given(userRepository.findById(any())).willReturn(Optional.of(mock(User.class)));
        given(groupMemberRequestRepository.findById(any())).willReturn(Optional.of(request));
        given(groupMemberRepository.existsByGroupAndMember(any(), any())).willReturn(false);
        given(groupMemberRepository.countByGroup(group)).willReturn(3L);

        //When
        Throwable t = catchThrowable(() -> groupMemberService.acceptMemberJoinRequest(groupMemberRequestId));

        //Then
        Assertions.assertThat(t)
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
        given(groupMemberRepository.existsByGroupAndMemberAndRole(any(), any(), any())).willReturn(true);

        //When
        groupMemberService.rejectMemberJoinRequest(groupMemberRequestId);

        //Then
        then(groupMemberRequestRepository).should().delete(any(GroupMemberRequest.class));
    }
}
