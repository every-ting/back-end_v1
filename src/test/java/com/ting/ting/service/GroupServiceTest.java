package com.ting.ting.service;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.dto.GroupDto;
import com.ting.ting.repository.GroupRepository;
import com.ting.ting.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.when;

@DisplayName("비즈니스 조직 - 과팅")
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @InjectMocks private GroupService groupService;

    @Mock private GroupRepository groupRepository;
    @Mock private UserRepository userRepository;

    @DisplayName("과팅 - 생성이 성공한 경우")
    @Test
    void givenGroupInfo_WhenSavingGroup_thenSavesGroup() {
        //Given
        GroupDto dto = createGroupDto();

        when(userRepository.getReferenceById((1L))).thenReturn(createUser());
        when(groupRepository.save(any(Group.class))).thenReturn(createGroup());

        // When & Then
        Assertions.assertDoesNotThrow(() -> groupService.saveGroup(createGroupDto()));
    }

    private Group createGroup() {
        return Group.of(
                createUser(),
                "팀 이름",
                Gender.M,
                "단국대학교",
                3,
                ""
        );
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
            1L,
            "username",
            "username@dankook.ac.kr",
            "단국대학교",
            "컴퓨터 공학과",
            Gender.M,
            LocalDate.now()
        );
    }
}