package com.ting.ting.service;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupMemberRequest;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.dto.GroupDto;
import com.ting.ting.repository.GroupMemberRepository;
import com.ting.ting.repository.GroupMemberRequestRepository;
import com.ting.ting.repository.GroupRepository;
import com.ting.ting.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스 조직 - 과팅")
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @InjectMocks private GroupService groupService;

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

        // When
        Page<GroupDto> groups = groupService.findAllGroups(pageable);

        // Then
        assertThat(groups).isEmpty();
    }

    @DisplayName("과팅 - 내가 속한 팀 조회")
    @Test
    void givenUserId_whenSearchingMyGroups_thenReturnsGroupSet() {
        //Given
        Long userId = 1L;
        User user = createUser(userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(groupMemberRepository.findGroupByMemberAndStatusAccepted(user)).willReturn(List.of(createGroup(2L)));

        // When
        Set<GroupDto> groups = groupService.findMyGroupList(userId);

        // Then
        assertThat(groups).hasSize(1);
    }

    @DisplayName("과팅 - 생성이 성공한 경우")
    @Test
    void givenUserIdAndGroupDto_WhenSavingGroup_thenSavesGroup() {
        //Given
        Long userId = 9L;
        GroupDto dto = createGroupDto();
        User user = createUser(userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(groupRepository.findByGroupName(dto.getGroupName())).willReturn(Optional.empty());
        given(groupRepository.save(any(Group.class))).willReturn(any(Group.class));

        // When & Then
        assertThatCode(() -> {
            groupService.saveGroup(userId, dto);
        }).doesNotThrowAnyException();
    }

    @DisplayName("과팅 - 멤버 가입 요청이 성공한 경우")
    @Test
    void givenGroupIdAndUserId_whenRequestingJoin_thenSavesRequest() {
        //Given
        Long groupId = 1L;
        Long userId = 2L;
        Group group = createGroup(groupId);
        User user = createUser(userId);

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(groupMemberRequestRepository.findByGroupAndUser(group, user)).willReturn(Optional.empty());
        given(groupMemberRequestRepository.save(any(GroupMemberRequest.class))).willReturn(null);

        // When
        groupService.saveJoinRequest(groupId, userId);

        // Then
        then(userRepository).should().findById(userId);
        then(groupRepository).should().findById(groupId);
        then(groupMemberRequestRepository).should().findByGroupAndUser(group, user);
        then(groupMemberRequestRepository).should().save(any(GroupMemberRequest.class));
    }

    @DisplayName("과팅 - 멤버 가입 요청을 취소")
    @Test
    void givenGroupIdAndUserId_whenCancelingJoinRequest_thenDeletesJoinRequest() {
        //Given
        Long groupId = 1L;
        Long userId = 2L;
        willDoNothing().given(groupMemberRequestRepository).deleteByGroup_IdAndUser_Id(groupId, userId);

        // When
        groupService.deleteJoinRequest(groupId, userId);

        // Then
        then(groupMemberRequestRepository).should().deleteByGroup_IdAndUser_Id(groupId, userId);
    }

    private Group createGroup() {
        return Group.of(
                createUser(4L),
                "팀 이름",
                Gender.M,
                "단국대학교",
                3,
                ""
        );
    }

    private Group createGroup(Long id) {
        Group group = createGroup();
        ReflectionTestUtils.setField(group, "id", id);
        return group;
    }

    private GroupDto createGroupDto() {
        return GroupDto.of(
                "팀 이름",
                Gender.M,
                3,
                "단국대학교",
                ""
        );
    }

    private User createUser() {
        return User.of(
            "username",
            "username@dankook.ac.kr",
            "단국대학교",
            "컴퓨터 공학과",
            Gender.M,
            LocalDate.now()
        );
    }

    private User createUser(Long id) {
        User user = createUser();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
