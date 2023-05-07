package com.ting.ting.service;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupMemberRequest;
import com.ting.ting.domain.User;
import com.ting.ting.dto.request.GroupRequest;
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

import java.util.List;
import java.util.Optional;

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
        given(groupMemberRepository.findAllGroupByMemberAndStatusActive(user)).willReturn(List.of(GroupFixture.entity(1L), GroupFixture.entity(2L)));

        // When & Then
        assertThat(groupService.findMyGroupList(userId)).hasSize(2);
    }

    @DisplayName("과팅 - 생성이 성공한 경우")
    @Test
    void givenUserIdAndGroupDto_WhenSavingGroup_thenSavesGroup() {
        //Given
        Long userId = 9L;
        GroupRequest request = GroupFixture.request();

        User leader = UserFixture.entity(userId);
        Group entity = request.toEntity(leader);

        given(userRepository.findById(userId)).willReturn(Optional.of(leader));
        given(groupRepository.findByGroupName(request.getGroupName())).willReturn(Optional.empty());
        given(groupRepository.save(any(Group.class))).willReturn(entity);

        // When
        GroupResponse actual = groupService.saveGroup(userId, request);

        // Then
        assertThat(actual.getGroupName()).isSameAs(entity.getGroupName());
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
}