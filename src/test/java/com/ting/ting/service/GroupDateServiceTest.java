package com.ting.ting.service;

import com.ting.ting.domain.*;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.domain.custom.GroupIdWithLikeCount;
import com.ting.ting.dto.response.DateableGroupResponse;
import com.ting.ting.dto.response.GroupDateRequestResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.TingApplicationException;
import com.ting.ting.fixture.GroupFixture;
import com.ting.ting.fixture.UserFixture;
import com.ting.ting.repository.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@DisplayName("[과팅] 과팅(date) 관련 비즈니스 로직 테스트")
@ExtendWith(MockitoExtension.class)
public class GroupDateServiceTest {

    @InjectMocks
    private GroupDateServiceImpl groupDateService;

    @Mock private UserRepository userRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private GroupLikeToDateRepository groupLikeToDateRepository;
    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private GroupDateRepository groupDateRepository;
    @Mock private GroupDateRequestRepository groupDateRequestRepository;

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

    @DisplayName("[팀장] : 과팅 요청 조회 기능 테스트")
    @Test
    void Given_Group_When_FindGroupDateRequests_Then_ReturnsGroupDateRequestResponseSet() {
        //Given
        Long groupId = 1L;
        Pageable pageable = Pageable.ofSize(20);

        Group myGroup = GroupFixture.createGroupById(groupId);
        Group fromGroup = GroupFixture.createGroupById(groupId + 1);
        GroupMember memberRecordOfUser = GroupMember.of(myGroup, user, MemberRole.MEMBER);
        ReflectionTestUtils.setField(fromGroup, "groupMembers", Set.of(GroupMember.of(fromGroup, UserFixture.createUserById(1L), MemberRole.LEADER)));
        GroupDateRequest dateRequest = GroupDateRequest.of(fromGroup, myGroup);
        ReflectionTestUtils.setField(dateRequest, "id", 1L);
        GroupIdWithLikeCount fromGroupIdWithLikeCount = new GroupIdWithLikeCount(fromGroup.getId(), 0L);

        given(groupRepository.findById(any())).willReturn(Optional.of(myGroup));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.findByGroupAndMember(myGroup, user)).willReturn(Optional.of(memberRecordOfUser));
        given(groupDateRequestRepository.findAllByFromGroup_IsMatchedAndToGroup(false, myGroup, pageable)).willReturn(new PageImpl<>(List.of(dateRequest)));
        given(groupLikeToDateRepository.findAllToGroupIdAndLikeCountByFromGroupMember_GroupAndToGroupIdsIn(myGroup, List.of(fromGroup))).willReturn(List.of(fromGroupIdWithLikeCount));
        given(groupRepository.findAllWithMembersInfoByIdIn(List.of(fromGroup.getId()))).willReturn(List.of(fromGroup));

        //When
        Page<DateableGroupResponse> created = groupDateService.findGroupDateRequests(groupId, pageable);

        //Then
        List<DateableGroupResponse> createdList = created.getContent().stream().collect(Collectors.toList());
        Assertions.assertThat(createdList).hasSize(1);
        Assertions.assertThat(createdList.get(0)).hasFieldOrPropertyWithValue("likeStatus", null);
        Assertions.assertThat(createdList.get(0)).hasFieldOrPropertyWithValue("requestStatus", RequestStatus.DISABLED);
        Assertions.assertThat(createdList.get(0)).hasFieldOrPropertyWithValue("likeCount", 0);
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
        given(groupMemberRepository.existsByGroupAndMemberAndRole(any(), any(), any())).willReturn(true);
        given(groupDateRequestRepository.existsByFromGroupAndToGroup(any(), any())).willReturn(false);
        given(groupDateRequestRepository.save(any())).willReturn(GroupDateRequest.of(fromGroup, toGroup));

        //When
        GroupDateRequestResponse created = groupDateService.saveGroupDateRequest(fromGroupId, toGroupId);

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
        Throwable t = catchThrowable(() -> groupDateService.saveGroupDateRequest(fromGroupId, toGroupId));

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
        given(groupMemberRepository.existsByGroupAndMemberAndRole(any(), any(), any())).willReturn(true);

        //When
        groupDateService.deleteGroupDateRequest(fromGroupId, toGroupId);

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
        given(groupMemberRepository.existsByGroupAndMemberAndRole(any(), any(), any())).willReturn(true);
        given(groupDateRepository.save(any())).willReturn(GroupDate.of(toGroup, fromGroup));

        //When
        groupDateService.acceptGroupDateRequest(groupDateRequestId);

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
        given(groupMemberRepository.existsByGroupAndMemberAndRole(any(), any(), any())).willReturn(true);
        given(groupDateRepository.existsByMenGroupOrWomenGroup(any(), any())).willReturn(true);

        //When
        Throwable t = catchThrowable(() -> groupDateService.acceptGroupDateRequest(groupDateRequestId));

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
        given(groupMemberRepository.existsByGroupAndMemberAndRole(any(), any(), any())).willReturn(true);

        //When & Then
        assertDoesNotThrow(() -> groupDateService.rejectGroupDateRequest(groupDateRequestId));
    }
}
