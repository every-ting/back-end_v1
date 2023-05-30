package com.ting.ting.service;

import com.ting.ting.domain.*;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.LikeStatus;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.DateableGroupResponse;
import com.ting.ting.dto.response.GroupDateRequestResponse;
import com.ting.ting.dto.response.GroupDateRequestWithFromAndToResponse;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("[과팅] 비즈니스 로직 테스트")
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @InjectMocks private GroupServiceImpl groupService;

    @Mock private UserRepository userRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private GroupMemberRequestRepository groupMemberRequestRepository;
    @Mock private GroupDateRepository groupDateRepository;
    @Mock private GroupDateRequestRepository groupDateRequestRepository;
    @Mock private GroupLikeToDateRepository groupLikeToDateRepository;
    @Mock private GroupLikeToJoinRepository groupLikeToJoinRepository;

    private User user;

    @BeforeEach
    private void setUpUser() {
        user = UserFixture.createUserById(1L);
    }

    @DisplayName("모든 팀 조회 성공")
    @Test
    void Given_Nothing_When_FindAllGroups_Then_ReturnsGroupResponsePage() {
        //Given
        Pageable pageable = Pageable.ofSize(20);
        given(groupRepository.findAllWithMemberCount(pageable)).willReturn(Page.empty());

        //When & Then
        assertThat(groupService.findAllGroups(pageable)).isEmpty();
    }

    @DisplayName("같은 성별 팀 가입을 위한 조회 기능 테스트")
    @Test
    void Given_Nothing_When_FindJoinableSameGenderGroupList_Then_ReturnsGroupWithRequestStatusResponsePage() {
        //Given
        Pageable pageable = Pageable.ofSize(20);

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupRepository.findAllJoinableGroupWithMemberCountByGenderAndIsJoinableAndNotGroupMembers_Member(any(), anyBoolean(), any(), any())).willReturn(Page.empty());
        given(groupMemberRequestRepository.findAllByUser(user)).willReturn(List.of());
        given(groupLikeToJoinRepository.findAllByFromUser(user)).willReturn(List.of());

        //When
        assertThat(groupService.findJoinableSameGenderGroupList(user.getId(), pageable)).isEmpty();
    }

    @DisplayName("다른 성별 팀 과팅 요청을 위한 조회 기능 테스트")
    @Test
    void Given_Group_When_FindDateableOppositeGenderGroupList_Then_ReturnsGroupWithLikeStatusResponse() {
        //Given
        Long groupId = 1L;
        Pageable pageable = Pageable.ofSize(20);

        Group myGroup = GroupFixture.createGroupById(groupId);
        Group oppositeGenderGroup = GroupFixture.createGroupById(groupId + 1);
        User oppositeGenderGroupMember = UserFixture.createUserById(user.getId() + 1);
        GroupMember oppositeGenderGroupMemberRecord = GroupMember.of(oppositeGenderGroup, oppositeGenderGroupMember, MemberRole.LEADER);
        ReflectionTestUtils.setField(oppositeGenderGroup, "groupMembers", Set.of(oppositeGenderGroupMemberRecord));

        given(groupRepository.findById(any())).willReturn(Optional.of(myGroup));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.findByGroupAndMember(any(), any())).willReturn(Optional.of(mock(GroupMember.class)));
        given(groupRepository.findAllByGenderAndIsJoinableAndIsMatchedAndMemberSizeLimit(myGroup.getGender().getOpposite(), false, false, myGroup.getMemberSizeLimit(), pageable))
                .willReturn(new PageImpl<>(List.of(oppositeGenderGroup)));
        given(groupRepository.findAllWithMembersInfoByIdIn(List.of(oppositeGenderGroup.getId()))).willReturn(List.of(oppositeGenderGroup));
        given(groupLikeToDateRepository.findAllByFromGroupMember(any())).willReturn(List.of());

        //When
        Page<DateableGroupResponse> created = groupService.findDateableOppositeGenderGroupList(groupId, user.getId(), pageable);

        //Then
        List<DateableGroupResponse> createdList = created.getContent().stream().collect(Collectors.toList());
        assertThat(createdList).hasSize(1);
        assertThat(createdList.get(0)).hasNoNullFieldsOrPropertiesExcept("requestStatus", "likeCount");
        assertThat(createdList.get(0)).hasFieldOrPropertyWithValue("likeStatus", LikeStatus.NOT_LIKED);
        assertThat(createdList.get(0).getGroup().getMajorsOfMembers()).hasSize(1);
    }

    @DisplayName("내가 속한 팀 조회 기능 테스트")
    @Test
    void Given_Nothing_When_FindMyGroupList_Then_ReturnsGroupSet() {
        //Given
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.findGroupWithMemberCountAndRoleByMember(user)).willReturn(List.of());

        //When & Then
        assertThat(groupService.findMyGroupList(user.getId())).hasSize(0);
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

    @DisplayName("[팀장] : 과팅 요청 조회 기능 테스트")
    @Test
    void Given_Group_When_FindAllGroupDateRequest_Then_ReturnsGroupDateRequestResponseSet() {
        //Given
        Long groupId = 1L;

        given(groupRepository.findById(any())).willReturn(Optional.of(mock(Group.class)));
        given(userRepository.findById(any())).willReturn(Optional.of(mock(User.class)));
        given(groupMemberRepository.existsByGroupAndMemberAndRole(any(), any(), any())).willReturn(true);
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
        given(groupMemberRepository.existsByGroupAndMemberAndRole(any(), any(), any())).willReturn(true);
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
        given(groupMemberRepository.existsByGroupAndMemberAndRole(any(), any(), any())).willReturn(true);

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
        given(groupMemberRepository.existsByGroupAndMemberAndRole(any(), any(), any())).willReturn(true);
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
        given(groupMemberRepository.existsByGroupAndMemberAndRole(any(), any(), any())).willReturn(true);
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
        given(groupMemberRepository.existsByGroupAndMemberAndRole(any(), any(), any())).willReturn(true);

        //When & Then
        assertDoesNotThrow(() -> groupService.rejectGroupDateRequest(user.getId(), groupDateRequestId));
    }
}