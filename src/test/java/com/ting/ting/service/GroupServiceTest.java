package com.ting.ting.service;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupMember;
import com.ting.ting.domain.GroupMemberRequest;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.MemberRole;
import com.ting.ting.domain.constant.MemberStatus;
import com.ting.ting.dto.request.GroupRequest;
import com.ting.ting.dto.response.GroupMemberResponse;
import com.ting.ting.dto.response.GroupResponse;
import com.ting.ting.fixture.GroupFixture;
import com.ting.ting.fixture.UserFixture;
import com.ting.ting.repository.GroupMemberRepository;
import com.ting.ting.repository.GroupMemberRequestRepository;
import com.ting.ting.repository.GroupRepository;
import com.ting.ting.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스 조직 - 과팅")
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @InjectMocks private GroupServiceImpl groupService;

    @Mock private GroupRepository groupRepository;
    @Mock private GroupMemberRepository groupMemberRepository;
    @Mock private GroupMemberRequestRepository groupMemberRequestRepository;
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
        User user = UserFixture.entity(userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(groupMemberRepository.findAllGroupByMemberAndStatus(user, MemberStatus.ACTIVE)).willReturn(List.of(GroupFixture.entity(1L), GroupFixture.entity(2L)));

        // When & Then
        assertThat(groupService.findMyGroupList(userId)).hasSize(2);
    }

    @DisplayName("과팅 - 팀 멤버 조회")
    @Test
    void givenGroupId_whenSearchingGroupMembers_thenReturnsGroupMemberSet() {
        //Given
        Long groupId = 1L;
        Group group = GroupFixture.entity(groupId);
        GroupMember groupMemberRecord1 = GroupMember.of(group, UserFixture.entity(1L), MemberStatus.ACTIVE, MemberRole.MEMBER);
        GroupMember groupMemberRecord2 = GroupMember.of(group, UserFixture.entity(2L), MemberStatus.ACTIVE, MemberRole.LEADER);

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

        User leader = UserFixture.entity(userId);
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

        Group group = GroupFixture.entity(groupId);
        User member = UserFixture.entity(memberId);
        User leader = UserFixture.entity(leaderId);
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

        Group group = GroupFixture.entity(groupId);
        User leader = UserFixture.entity(leaderId);
        GroupMember groupLeaderRecord = GroupMember.of(group, leader, MemberStatus.ACTIVE, MemberRole.LEADER);
        GroupMemberRequest groupMemberRequest1 = GroupMemberRequest.of(group, UserFixture.entity(2L));
        GroupMemberRequest groupMemberRequest2 = GroupMemberRequest.of(group, UserFixture.entity(3L));

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(userRepository.findById(leaderId)).willReturn(Optional.of(leader));
        given(groupMemberRepository.findByGroupAndRole(group, MemberRole.LEADER)).willReturn(Optional.of(groupLeaderRecord));
        given(groupMemberRequestRepository.findByGroup(group)).willReturn(List.of(groupMemberRequest1, groupMemberRequest2));

        // When & Then
        assertThat(groupService.findMemberJoinRequest(groupId, leaderId)).hasSize(2);
    }

    @DisplayName("과팅 - [팀장] : 멤버 가입 요청 수락")
    @Test
    void givenLeaderIdAndGroupMemberRequestId_whenAcceptingMemberJoinRequest_thenReturnsCreatedGroupMemberResponse() {
        //Given
        Long leaderId = 1L;
        Long groupMemberRequestId = 1L;

        User leader = UserFixture.entity(leaderId);
        User member = UserFixture.entity(leaderId + 1);
        Group group = GroupFixture.entity(1L);
        GroupMemberRequest groupMemberRequest = GroupMemberRequest.of(group, member);
        GroupMember groupMember = GroupMember.of(group, member, MemberStatus.ACTIVE, MemberRole.MEMBER);

        given(userRepository.findById(leaderId)).willReturn(Optional.of(leader));
        given(groupMemberRequestRepository.findById(groupMemberRequestId)).willReturn(Optional.of(groupMemberRequest));
        given(groupMemberRepository.findGroupByMemberAndRole(leader, MemberRole.LEADER)).willReturn(Optional.of(group));
        given(groupMemberRepository.save(any())).willReturn(groupMember);

        // When
        GroupMemberResponse actual = groupService.acceptMemberJoinRequest(leaderId, groupMemberRequestId);

        // Then
        assertThat(actual.getMember().getUsername()).isSameAs(member.getUsername());
        then(groupMemberRepository).should().save(any(GroupMember.class));
        then(groupMemberRequestRepository).should().delete(any());
    }
}