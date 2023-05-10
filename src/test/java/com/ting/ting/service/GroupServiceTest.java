package com.ting.ting.service;

import com.ting.ting.domain.*;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.MemberStatus;
import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.GroupDateResponse;
import com.ting.ting.dto.response.GroupMemberResponse;
import com.ting.ting.dto.response.GroupResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.TingApplicationException;
import com.ting.ting.fixture.GroupFixture;
import com.ting.ting.fixture.UserFixture;
import com.ting.ting.repository.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스 조직 - 과팅")
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @InjectMocks private GroupServiceImpl groupService;

    @Mock private GroupRepository groupRepository;
    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private GroupMemberRequestRepository groupMemberRequestRepository;
    @Mock private GroupDateRepository groupDateRepository;
    @Mock private GroupDateRequestRepository groupDateRequestRepository;
    @Mock private UserRepository userRepository;

    @DisplayName("과팅 - 모든 팀 조회")
    @Test
    void givenNothing_whenSearchingAllGroups_thenReturnsGroupPage() {
        //Given
        Pageable pageable = Pageable.ofSize(20);
        given(groupRepository.findAll(pageable)).willReturn(Page.empty());

        // When & Then
        assertThat(groupService.findAllGroups(pageable)).isEmpty();
    }

    @DisplayName("과팅 - 내가 속한 팀 조회")
    @Test
    void givenUserId_whenSearchingMyGroups_thenReturnsGroupSet() {
        //Given
        Long userId = 1L;
        User user = UserFixture.entityById(userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(groupMemberRepository.findAllGroupByMemberAndStatus(user, MemberStatus.ACTIVE)).willReturn(List.of(GroupFixture.entityById(1L), GroupFixture.entityById(2L)));

        // When & Then
        assertThat(groupService.findMyGroupList(userId)).hasSize(2);
    }

    @DisplayName("과팅 - 팀 멤버 조회")
    @Test
    void givenGroupId_whenSearchingGroupMembers_thenReturnsGroupMemberSet() {
        //Given
        Long groupId = 1L;
        Group group = GroupFixture.entityById(groupId);
        GroupMember groupMemberRecord1 = GroupMember.of(group, UserFixture.entityById(1L), MemberStatus.ACTIVE, MemberRole.MEMBER);
        GroupMember groupMemberRecord2 = GroupMember.of(group, UserFixture.entityById(2L), MemberStatus.ACTIVE, MemberRole.LEADER);

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(groupMemberRepository.findAllByGroup(group)).willReturn(List.of(groupMemberRecord1, groupMemberRecord2));

        // When & Then
        assertThat(groupService.findGroupMemberList(groupId)).hasSize(2);
    }

    @DisplayName("과팅 - 생성이 성공한 경우")
    @Test
    void givenUserIdAndGroupDto_WhenSavingGroup_thenSavesGroup() {
        //Given
        Long userId = 9L;
        GroupRequest request = GroupFixture.request();

        User leader = UserFixture.entityById(userId);
        Group entity = request.toEntity();

        given(userRepository.findById(userId)).willReturn(Optional.of(leader));
        given(groupRepository.findByGroupName(request.getGroupName())).willReturn(Optional.empty());
        given(groupRepository.save(any(Group.class))).willReturn(entity);

        // When
        GroupResponse actual = groupService.saveGroup(userId, request);

        // Then
        assertThat(actual.getGroupName()).isSameAs(entity.getGroupName());
        then(groupMemberRepository).should().save(any(GroupMember.class));
    }

    @DisplayName("과팅 - 멤버 가입 요청 기능 테스트")
    @Test
    void givenGroupIdAndUserId_whenRequestingJoin_thenSavesRequest() {
        //Given
        Long groupId = 1L;
        Long userId = 3L;

        given(groupRepository.findById(groupId)).willReturn(Optional.of(mock(Group.class)));
        given(userRepository.findById(userId)).willReturn(Optional.of(mock(User.class)));
        given(groupMemberRequestRepository.findByGroupAndUser(any(), any())).willReturn(Optional.empty());
        given(groupMemberRequestRepository.save(any())).willReturn(any(GroupMemberRequest.class));

        // When & Then
        Assertions.assertDoesNotThrow(() -> groupService.saveJoinRequest(groupId, userId));
    }

    @DisplayName("과팅 - 멤버 가입 요청을 취소")
    @Test
    void givenGroupIdAndUserId_whenCancelingJoinRequest_thenDeletesJoinRequest() {
        //Given
        Long groupId = 1L;
        Long userId = 2L;

        willDoNothing().given(groupMemberRequestRepository).deleteByGroup_IdAndUser_Id(groupId, userId);

        // When & Then
        Assertions.assertDoesNotThrow(() -> groupService.deleteJoinRequest(groupId, userId));
    }

    @DisplayName("과팅 - [팀장] : 팀장 넘기기 기능 테스트")
    @Test
    void givenGroupIdAndLeaderIdAndMemberId_whenChangingLeaderRequest_thenChangesLeader() {
        //Given
        Long groupId = 1L;
        Long memberId = 1L;
        Long leaderId = 2L;

        Group group = GroupFixture.entityById(groupId);
        User member = UserFixture.entityById(memberId);
        User leader = UserFixture.entityById(leaderId);
        GroupMember groupMemberRecord = GroupMember.of(group, member, MemberStatus.ACTIVE, MemberRole.MEMBER);
        GroupMember groupLeaderRecord = GroupMember.of(group, leader, MemberStatus.ACTIVE, MemberRole.LEADER);

        HashMap<Long, MemberRole> map = new LinkedHashMap<>();
        map.put(memberId, groupMemberRecord.getRole());
        map.put(leaderId, groupLeaderRecord.getRole());

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(userRepository.findById(leaderId)).willReturn(Optional.of(leader));
        given(userRepository.findById(memberId)).willReturn(Optional.of(member));
        given(groupMemberRepository.findByGroupAndMemberAndStatus(group, leader, MemberStatus.ACTIVE)).willReturn(Optional.of(groupLeaderRecord));
        given(groupMemberRepository.findByGroupAndMemberAndStatus(group, member, MemberStatus.ACTIVE)).willReturn(Optional.of(groupMemberRecord));
        given(groupMemberRepository.saveAllAndFlush(List.of(groupLeaderRecord, groupMemberRecord))).willReturn(List.of(groupLeaderRecord, groupMemberRecord));

        // When
        Set<GroupMemberResponse> actual = groupService.changeGroupLeader(groupId, leaderId, memberId);

        // Then
        Iterator<GroupMemberResponse> iter = actual.iterator();
        GroupMemberResponse response = iter.next();
        assertThat(map.get(response.getMember().getId())).isNotSameAs(response.getRole());
    }

    @DisplayName("과팅 - [팀장] : 멤버 가입 요청 조회")
    @Test
    void givenGroupIdAndLeaderId_whenSearchingMemberJoinRequest_thenReturnsGroupMemberRequestSet() {
        //Given
        Long groupId = 1L;
        Long leaderId = 1L;

        Group group = GroupFixture.entityById(groupId);
        User leader = UserFixture.entityById(leaderId);
        GroupMember memberRecordOfLeader = GroupMember.of(group, leader, MemberStatus.ACTIVE, MemberRole.LEADER);
        GroupMemberRequest groupMemberRequest1 = GroupMemberRequest.of(group, UserFixture.entityById(2L));
        GroupMemberRequest groupMemberRequest2 = GroupMemberRequest.of(group, UserFixture.entityById(3L));

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(userRepository.findById(leaderId)).willReturn(Optional.of(leader));
        given(groupMemberRepository.findByGroupAndRole(group, MemberRole.LEADER)).willReturn(Optional.of(memberRecordOfLeader));
        given(groupMemberRequestRepository.findByGroup(group)).willReturn(List.of(groupMemberRequest1, groupMemberRequest2));

        // When & Then
        assertThat(groupService.findMemberJoinRequest(groupId, leaderId)).hasSize(2);
    }

    @DisplayName("과팅 - [팀장] : 멤버 가입 요청 수락 성공")
    @Test
    void givenLeaderIdAndGroupMemberRequestId_whenAcceptingMemberJoinRequest_thenReturnsCreatedGroupMemberResponse() {
        //Given
        Long leaderId = 1L;
        Long groupMemberRequestId = 1L;

        User leader = UserFixture.entityById(leaderId);
        User member = UserFixture.entityById(leaderId + 1);
        Group group = GroupFixture.entityById(1L);
        GroupMemberRequest groupMemberRequest = GroupMemberRequest.of(group, member);
        GroupMember memberRecordOfLeader = GroupMember.of(group, leader, MemberStatus.ACTIVE, MemberRole.LEADER);
        GroupMember groupMember = GroupMember.of(group, member, MemberStatus.ACTIVE, MemberRole.MEMBER);

        given(userRepository.findById(leaderId)).willReturn(Optional.of(leader));
        given(groupMemberRequestRepository.findById(groupMemberRequestId)).willReturn(Optional.of(groupMemberRequest));
        given(groupMemberRepository.findByGroupAndRole(group, MemberRole.LEADER)).willReturn(Optional.of(memberRecordOfLeader));
        given(groupMemberRepository.existsByGroupAndMember(group, member)).willReturn(false);
        given(groupMemberRepository.save(any())).willReturn(groupMember);

        // When
        GroupMemberResponse actual = groupService.acceptMemberJoinRequest(leaderId, groupMemberRequestId);

        // Then
        assertThat(actual.getMember().getUsername()).isSameAs(member.getUsername());
        then(groupMemberRepository).should().save(any(GroupMember.class));
        then(groupMemberRequestRepository).should().delete(any());
    }

    @DisplayName("과팅 - [팀장] : 멤버 가입 요청 수락 에러 - 요청한 유저가 이미 멤버인 경우")
    @Test
    void givenLeaderIdAndGroupMemberRequestIdWhoIsAlreadyMemberOfGroup_whenAcceptingMemberJoinRequest_thenThrows() {
        //Given
        Long leaderId = 1L;
        Long groupMemberRequestId = 1L;

        User leader = UserFixture.entityById(leaderId);
        User member = UserFixture.entityById(leaderId + 1);
        Group group = GroupFixture.entityById(1L);
        GroupMemberRequest groupMemberRequest = GroupMemberRequest.of(group, member);

        given(userRepository.findById(leaderId)).willReturn(Optional.of(leader));
        given(groupMemberRequestRepository.findById(groupMemberRequestId)).willReturn(Optional.of(groupMemberRequest));
        given(groupMemberRepository.existsByGroupAndMember(group, member)).willReturn(true);

        // When
        Throwable t = catchThrowable(() ->  groupService.acceptMemberJoinRequest(leaderId, groupMemberRequestId));

        // Then
        assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATED_REQUEST);
    }

    @DisplayName("과팅 - [팀장] : 멤버 가입 요청 수락 에러 - 팀의 가능한 멤버수가 꽉찬 경우")
    @Test
    void givenGroupInfoWhichHasNoCapacityForNewMember_whenAcceptingMemberJoinRequest_thenThrows() {
        //Given
        Long leaderId = 1L;
        Long groupMemberRequestId = 1L;

        User leader = UserFixture.entityById(leaderId);
        User member = UserFixture.entityById(leaderId + 1);
        Group group = GroupFixture.entityById(1L);
        group.setNumOfMember(2);
        GroupMemberRequest groupMemberRequest = GroupMemberRequest.of(group, member);

        given(userRepository.findById(leaderId)).willReturn(Optional.of(leader));
        given(groupMemberRequestRepository.findById(groupMemberRequestId)).willReturn(Optional.of(groupMemberRequest));
        given(groupMemberRepository.existsByGroupAndMember(group, member)).willReturn(false);
        given(groupMemberRepository.countByGroup(group)).willReturn(2L);

        // When
        Throwable t = catchThrowable(() ->  groupService.acceptMemberJoinRequest(leaderId, groupMemberRequestId));

        // Then
        assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REACHED_MEMBERS_SIZE_LIMIT);
    }

    @DisplayName("과팅 - [팀장] : 멤버 가입 요청 거절")
    @Test
    void givenLeaderIdAndGroupMemberRequestId_whenRejectingMemberJoinRequest_thenDeletesMemberJoinRequest() {
        //Given
        Long leaderId = 1L;
        Long groupMemberRequestId = 1L;

        User leader = UserFixture.entityById(leaderId);
        Group group = GroupFixture.entityById(1L);
        GroupMemberRequest groupMemberRequest = GroupMemberRequest.of(group, leader);
        GroupMember memberRecordOfLeader = GroupMember.of(group, leader, MemberStatus.ACTIVE, MemberRole.LEADER);

        given(userRepository.findById(leaderId)).willReturn(Optional.of(leader));
        given(groupMemberRequestRepository.findById(groupMemberRequestId)).willReturn(Optional.of(groupMemberRequest));
        given(groupMemberRepository.findByGroupAndRole(group, MemberRole.LEADER)).willReturn(Optional.of(memberRecordOfLeader));

        // When
        groupService.rejectMemberJoinRequest(leaderId, groupMemberRequestId);

        // Then
        then(groupMemberRequestRepository).should().delete(any());
    }

    @DisplayName("과팅 - [팀장] : 과팅 요청 조회")
    @Test
    void givenLeaderIdAndGroupId_whenSearchingGroupDateRequest_thenReturnsGroupDateRequestResponseSet() {
        //Given
        Long leaderId = 1L;
        Long groupId = 1L;

        User leader = UserFixture.entityById(leaderId);
        Group group = GroupFixture.entityById(groupId);
        GroupMember memberRecordOfLeader = GroupMember.of(group, leader, MemberStatus.ACTIVE, MemberRole.LEADER);
        GroupDateRequest groupDateRequest1 = GroupDateRequest.of(GroupFixture.entityById(2L), group);
        GroupDateRequest groupDateRequest2 = GroupDateRequest.of(GroupFixture.entityById(3L), group);

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(userRepository.findById(leaderId)).willReturn(Optional.of(leader));
        given(groupMemberRepository.findByGroupAndRole(group, MemberRole.LEADER)).willReturn(Optional.of(memberRecordOfLeader));
        given(groupDateRequestRepository.findByToGroup(group)).willReturn(List.of(groupDateRequest1, groupDateRequest2));

        // When & Then
        assertThat(groupService.findAllGroupDateRequest(groupId, leaderId)).hasSize(2);
    }

    @DisplayName("과팅 - [팀장] : 과팅 요청 수락 성공")
    @Test
    void givenLeaderIdAndGroupDateRequestId_whenAcceptingGroupDateRequest_thenReturnsCreatedGroupDateResponse() {
        //Given
        Long leaderId = 1L;
        Long groupDateRequestId = 1L;

        User leader = UserFixture.entityById(leaderId);
        Group menGroup = GroupFixture.entityByGender(Gender.MEN);
        Group womenGroup = GroupFixture.entityByGender(Gender.WOMEN);
        GroupDateRequest groupDateRequest = GroupDateRequest.of(menGroup, womenGroup);
        GroupMember memberRecordOfLeader = GroupMember.of(groupDateRequest.getToGroup(), leader, MemberStatus.ACTIVE, MemberRole.LEADER);
        GroupDate expected = GroupDate.of(menGroup, womenGroup);
        ReflectionTestUtils.setField(expected, "id", 1L);

        given(userRepository.findById(leaderId)).willReturn(Optional.of(leader));
        given(groupDateRequestRepository.findById(groupDateRequestId)).willReturn(Optional.of(groupDateRequest));
        given(groupMemberRepository.findByGroupAndRole(groupDateRequest.getToGroup(), MemberRole.LEADER)).willReturn(Optional.of(memberRecordOfLeader));
        given(groupDateRepository.existsByMenGroupOrWomenGroup(menGroup, womenGroup)).willReturn(false);
        given(groupDateRepository.save(any())).willReturn(expected);

        // When
        GroupDateResponse actual = groupService.acceptGroupDateRequest(leaderId, groupDateRequestId);

        // Then
        assertThat(actual.getMenGroup().getId()).isSameAs(menGroup.getId());
        assertThat(actual.getWomenGroup().getId()).isSameAs(womenGroup.getId());
        assertThat(actual.getMenGroup().isMatched()).isSameAs(true);
        assertThat(actual.getWomenGroup().isMatched()).isSameAs(true);
        then(groupDateRepository).should().save(any(GroupDate.class));
        then(groupDateRequestRepository).should().delete(any());
    }

    @DisplayName("과팅 - [팀장] : 과팅 요청 수락 에러- 이미 매칭된 과팅이 있는 경우")
    @Test
    void givenLeaderIdAndGroupDateRequestIdWhichContainsAlreadyMatchedGroupMeeting_whenAcceptingGroupDateRequest_thenThrows() {
        //Given
        Long leaderId = 1L;
        Long groupDateRequestId = 1L;

        User leader = UserFixture.entityById(leaderId);
        Group menGroup = GroupFixture.entityByGender(Gender.MEN);
        Group womenGroup = GroupFixture.entityByGender(Gender.WOMEN);
        GroupDateRequest groupDateRequest = GroupDateRequest.of(menGroup, womenGroup);

        given(userRepository.findById(any())).willReturn(Optional.of(leader));
        given(groupDateRequestRepository.findById(groupDateRequestId)).willReturn(Optional.of(groupDateRequest));
        given(groupDateRepository.existsByMenGroupOrWomenGroup(menGroup, womenGroup)).willReturn(true);

        // When
        Throwable t = catchThrowable(() -> groupService.acceptGroupDateRequest(leaderId, groupDateRequestId));

        // Then
        assertThat(t)
                .isInstanceOf(TingApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATED_REQUEST);
    }

    @DisplayName("과팅 - [팀장] : 과팅 요청 삭제")
    @Test
    void givenLeaderIdAndGroupDateRequestId_whenRejectingGroupDateRequest_thenDeletedGroupDateRequestRecord() {
        //Given
        Long leaderId = 1L;
        Long groupDateRequestId = 1L;

        User leader = UserFixture.entityById(leaderId);
        GroupDateRequest groupDateRequest = GroupDateRequest.of(GroupFixture.entityById(1L), GroupFixture.entityById(1L));
        GroupMember memberRecordOfLeader = GroupMember.of(groupDateRequest.getToGroup(), leader, MemberStatus.ACTIVE, MemberRole.LEADER);

        given(userRepository.findById(leaderId)).willReturn(Optional.of(leader));
        given(groupDateRequestRepository.findById(groupDateRequestId)).willReturn(Optional.of(groupDateRequest));
        given(groupMemberRepository.findByGroupAndRole(groupDateRequest.getToGroup(), MemberRole.LEADER)).willReturn(Optional.of(memberRecordOfLeader));

        // When
        groupService.rejectGroupDateRequest(leaderId, groupDateRequestId);

        // Then
        then(groupDateRequestRepository).should().delete(any());
    }
}