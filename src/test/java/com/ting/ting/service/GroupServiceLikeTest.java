package com.ting.ting.service;

import com.ting.ting.domain.*;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.LikeStatus;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.RequestStatus;
import com.ting.ting.domain.custom.GroupIdWithLikeCount;
import com.ting.ting.domain.custom.GroupWithMemberCount;
import com.ting.ting.dto.response.DateableGroupResponse;
import com.ting.ting.dto.response.JoinableGroupResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@DisplayName("[과팅] 찜하기 관련 비즈니스 로직 테스트")
@ExtendWith(MockitoExtension.class)
class GroupLikeServiceTest {

    @InjectMocks private GroupLikeServiceImpl groupLikeService;

    @Mock private UserRepository userRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private GroupMemberRequestRepository groupMemberRequestRepository;
    @Mock private GroupDateRequestRepository groupDateRequestRepository;
    @Mock private GroupLikeToJoinRepository groupLikeToJoinRepository;
    @Mock private GroupLikeToDateRepository groupLikeToDateRepository;

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

    @DisplayName("팀 기준 - 찜한 목록 조회 기능 테스트")
    @Test
    void Given_Group_When_FindGroupLikeToDateList_Then_ReturnsDateableGroupResponse() {
        //Given
        Long groupId = 1L;
        Pageable pageable = PageRequest.of(0, 20);

        Group fromGroup = GroupFixture.createGroupById(groupId);
        Group toGroup = GroupFixture.createGroupById(groupId + 1);
        User toGroupMember = UserFixture.createUserById(user.getId() + 1);
        ReflectionTestUtils.setField(toGroupMember, "birth", LocalDate.ofYearDay(LocalDate.now().getYear() - 10, LocalDate.now().getDayOfMonth()));
        GroupMember fromGroupMemberRecord = GroupMember.of(fromGroup, user, MemberRole.MEMBER);
        GroupMember toGroupMemberRecord = GroupMember.of(toGroup, toGroupMember, MemberRole.LEADER);
        ReflectionTestUtils.setField(toGroup, "groupMembers", Set.of(toGroupMemberRecord));
        ReflectionTestUtils.setField(toGroup, "isMatched", true);
        GroupLikeToDate groupLikeToDateRecord = GroupLikeToDate.of(fromGroupMemberRecord, toGroup);

        given(groupRepository.findById(groupId)).willReturn(Optional.of(fromGroup));
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(groupMemberRepository.findByGroupAndMember(fromGroup, user)).willReturn(Optional.of(fromGroupMemberRecord));
        given(groupLikeToDateRepository.findAllToGroupIdAndLikeCountByFromGroupMember_Group(fromGroup, pageable)).willReturn(new PageImpl<>(List.of(new GroupIdWithLikeCount(toGroup.getId(), 2L))));
        given(groupRepository.findAllWithMembersInfoByIdIn(List.of(groupLikeToDateRecord.getToGroup().getId()))).willReturn(List.of(toGroup));
        given(groupDateRequestRepository.findAllByFromGroup(fromGroup)).willReturn(List.of());
        given(groupLikeToDateRepository.findAllByFromGroupMember(fromGroupMemberRecord)).willReturn(List.of());

        //When
        Page<DateableGroupResponse> created = groupLikeService.findGroupLikeToDateList(groupId, pageable);

        //Then
        List<DateableGroupResponse> createdList = created.getContent().stream().collect(Collectors.toList());
        assertThat(createdList).hasSize(1);
        assertThat(createdList.get(0)).hasFieldOrPropertyWithValue("requestStatus", RequestStatus.DISABLED);
        assertThat(createdList.get(0)).hasFieldOrPropertyWithValue("likeStatus", LikeStatus.NOT_LIKED);
        assertThat(createdList.get(0)).hasFieldOrPropertyWithValue("likeCount", 2);
        assertThat(createdList.get(0).getGroup().getMajorsOfMembers()).hasSize(1);
        assertThat(createdList.get(0).getGroup().getAverageAgeOfMembers()).isSameAs(10);
    }

    @DisplayName("유저 기준 - 같은 성별 팀 찜한 목록 조회 기능 테스트")
    @Test
    void Given_Nothing_WHen_FindGroupLikeToJoinList_Then_ReturnsJoinableGroupResponse() {
        //Given
        Pageable pageable = PageRequest.of(0, 20);

        Group toGroup1 = GroupFixture.createGroupById(1L);
        Group toGroup2 = GroupFixture.createGroupById(2L);
        GroupLikeToJoin groupLikeToJoin1 = GroupLikeToJoin.of(user, toGroup1);
        GroupLikeToJoin groupLikeToJoin2 = GroupLikeToJoin.of(user, toGroup2);
        GroupWithMemberCount joinableGroupWithMemberCount = new GroupWithMemberCount(toGroup1.getId(), toGroup1.getGroupName(), toGroup1.getGender(), 2L, toGroup1.getMemberSizeLimit(), toGroup1.getSchool(), toGroup1.isMatched(), true, toGroup1.getMemo(), toGroup1.getIdealPhoto(), toGroup1.getCreatedAt());
        GroupWithMemberCount notJoinableGroupWithMemberCount = new GroupWithMemberCount(toGroup2.getId(), toGroup2.getGroupName(), toGroup2.getGender(), 2L, toGroup2.getMemberSizeLimit(), toGroup2.getSchool(), toGroup2.isMatched(), false, toGroup2.getMemo(), toGroup2.getIdealPhoto(), toGroup2.getCreatedAt());

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(groupLikeToJoinRepository.findAllByFromUser(user, pageable)).willReturn(new PageImpl<>(List.of(groupLikeToJoin1, groupLikeToJoin2)));
        given(groupRepository.findAllWithMemberCountByIdIn(any())).willReturn(List.of(joinableGroupWithMemberCount, notJoinableGroupWithMemberCount));
        given(groupMemberRequestRepository.findAllByUser(user)).willReturn(List.of(GroupMemberRequest.of(toGroup1, user)));

        //When
        Page<JoinableGroupResponse> created = groupLikeService.findGroupLikeToJoinList(pageable);

        //Then
        List<JoinableGroupResponse> createdList = created.getContent().stream().collect(Collectors.toList());
        assertThat(createdList).hasSize(2);
        assertThat(createdList.get(0)).hasFieldOrPropertyWithValue("requestStatus", RequestStatus.PENDING);
        assertThat(createdList.get(0)).hasFieldOrPropertyWithValue("likeStatus", LikeStatus.LIKED);
        assertThat(createdList.get(1)).hasFieldOrPropertyWithValue("requestStatus", RequestStatus.DISABLED);
        assertThat(createdList.get(1)).hasFieldOrPropertyWithValue("likeStatus", LikeStatus.LIKED);
    }

    @DisplayName("같은 성별의 팀 찜하기 기능 테스트")
    @Test
    void Given_Group_When_CreateSameGenderGroupLike_Then_SavesGroupLikeToJoin() {
        //Given
        Long groupId = 1L;

        ReflectionTestUtils.setField(user, "gender", Gender.WOMEN);
        Group group = GroupFixture.createGroupById(groupId);
        ReflectionTestUtils.setField(group, "gender", Gender.WOMEN);

        given(groupRepository.findById(any())).willReturn(Optional.of(group));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupLikeToJoinRepository.existsByFromUserAndToGroup(any(), any())).willReturn(false);

        //When
        groupLikeService.createSameGenderGroupLike(groupId);

        //Then
        then(groupLikeToJoinRepository).should().save(any());
    }

    @DisplayName("같은 성별의 팀 찜하기 취소 기능 테스트")
    @Test
    void Given_Group_When_DeleteSameGenderGroupLike_Then_DeletesGroupLikeToJoin() {
        //Given
        Long groupId = 1L;

        //When
        groupLikeService.deleteSameGenderGroupLike(groupId);

        //Then
        then(groupLikeToJoinRepository).should().deleteByFromUser_IdAndToGroup_Id(any(), any());
        then(groupLikeToJoinRepository).shouldHaveNoMoreInteractions();
    }

    @DisplayName("다른 성별의 팀 찜하기 기능 테스트")
    @Test
    void Given_FromGroupAndToGroup_When_CreateOppositeGenderGroupLike_Then_SavesGroupLikeToDate() {
        //Given
        Long fromGroupId = 1L;
        Long toGroupId = 2L;

        Group fromGroup = GroupFixture.createGroupById(fromGroupId);
        ReflectionTestUtils.setField(fromGroup, "gender", Gender.WOMEN);
        Group toGroup = GroupFixture.createGroupById(toGroupId);
        ReflectionTestUtils.setField(toGroup, "gender", Gender.MEN);

        given(groupRepository.findById(any())).willReturn(Optional.of(fromGroup)).willReturn(Optional.of(toGroup));
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(groupMemberRepository.findByGroupAndMember(any(), any())).willReturn(Optional.of(mock(GroupMember.class)));
        given(groupLikeToDateRepository.existsByFromGroupMemberAndToGroup(any(), any())).willReturn(false);

        //When
        groupLikeService.createOppositeGenderGroupLike(fromGroupId, toGroupId);

        //Then
        then(groupLikeToDateRepository).should().save(any());
    }

    @DisplayName("다른 성별의 팀 찜하기 취소 기능 테스트")
    @Test
    void Given_FromGroupAndToGroup_When_DeleteOppositeGenderGroupLike_Then_DeletesGroupLikeToDate() {
        //Given
        Long fromGroupId = 1L;
        Long toGroupId = 2L;

        given(groupRepository.findById(any())).willReturn(Optional.of(mock(Group.class))).willReturn(Optional.of(mock(Group.class)));
        given(userRepository.findById(any())).willReturn(Optional.of(mock(User.class)));
        given(groupMemberRepository.findByGroupAndMember(any(), any())).willReturn(Optional.of(mock(GroupMember.class)));

        //When
        groupLikeService.deleteOppositeGenderGroupLike(fromGroupId, toGroupId);

        //Then
        then(groupLikeToDateRepository).should().deleteByFromGroupMemberAndToGroup(any(), any());
    }

}