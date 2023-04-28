package com.ting.ting.service;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.dto.GroupDto;
import com.ting.ting.repository.GroupRepository;
import com.ting.ting.repository.UserRepository;
import org.assertj.core.api.InstanceOfAssertFactories;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@DisplayName("비즈니스 조직 - 과팅")
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @InjectMocks private GroupService groupService;

    @Mock private GroupRepository groupRepository;
    @Mock private UserRepository userRepository;

    @DisplayName("과팅 - 팀 조회")
    @Test
    void givenNothing_whenSearchingGroups_thenReturnsGroupPage() {
        //Given
        Pageable pageable = Pageable.ofSize(20);
        given(groupRepository.findAll(pageable)).willReturn(Page.empty());

        // When
        Page<GroupDto> groups = groupService.list(pageable);

        // Then
        assertThat(groups).isEmpty();
    }

    @DisplayName("과팅 - 생성이 성공한 경우")
    @Test
    void givenGroupInfo_WhenSavingGroup_thenSavesGroup() {
        //Given
        GroupDto dto = createGroupDto();

        given(userRepository.getReferenceById((9L))).willReturn(createUser(9L));
        given(groupRepository.save(any(Group.class))).willReturn(createGroup());

        // When & Then
        assertThatCode(() -> {
            groupService.saveGroup(createGroupDto());
        }).doesNotThrowAnyException();
    }

    @DisplayName("과팅 - 멤버 가입 요청이 성공한 경우")
    @Test
    void givenGroupId_whenRequestingJoin_thenRequests() {
        //Given
        Group group = createGroup(1L);
        User user = createUser(2L);

        given(userRepository.getReferenceById(user.getId())).willReturn(user);
        given(groupRepository.getReferenceById(group.getId())).willReturn(group);

        // When
        groupService.createJoinRequest(group.getId());

        // Then
        assertThat(group)
                .extracting("joinRequests", as(InstanceOfAssertFactories.COLLECTION))
                .hasSize(1)
                .extracting("id")
                .containsExactly(user.getId());
    }

    @DisplayName("과팅 - 멤버 가입 요청을 취소")
    @Test
    void givenGroupId_whenCancelingJoinRequest_thenDeletesJoinRequest() {
        //Given
        Group group = createGroup(1L);
        User request1 = createUser(2L);
        User request2 = createUser(3L);
        group.addJoinRequests(request1);
        group.addJoinRequests(request2);
        given(userRepository.getReferenceById(request1.getId())).willReturn(request1);
        given(groupRepository.getReferenceById(group.getId())).willReturn(group);

        // When
        groupService.deleteJoinRequest(group.getId());

        // Then
        assertThat(group.getJoinRequests())
                .hasSize(1)
                .extracting("id")
                .containsExactly(request2.getId());
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

    private User createUser(Long id) {
        return User.of(
            id,
            "username",
            "username@dankook.ac.kr",
            "단국대학교",
            "컴퓨터 공학과",
            Gender.M,
            LocalDate.now()
        );
    }
}