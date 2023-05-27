package com.ting.ting.service;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.fixture.GroupFixture;
import com.ting.ting.fixture.UserFixture;
import com.ting.ting.repository.GroupLikeToJoinRepository;
import com.ting.ting.repository.GroupRepository;
import com.ting.ting.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("[과팅] 찜하기 관련 비즈니스 로직 테스트")
@ExtendWith(MockitoExtension.class)
class GroupLikeServiceTest {

    @InjectMocks private GroupLikeServiceImpl groupLikeService;

    @Mock private UserRepository userRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private GroupLikeToJoinRepository groupLikeToJoinRepository;

    private User user;

    @BeforeEach
    private void setUpUser() {
        user = UserFixture.createUserById(1L);
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
        groupLikeService.createSameGenderGroupLike(groupId, user.getId());

        //Then
        then(groupLikeToJoinRepository).should().save(any());
    }

    @DisplayName("같은 성별의 팀 찜하기 취소 기능 테스트")
    @Test
    void Given_Group_When_DeleteSameGenderGroupLike_Then_DeletesGroupLikeToJoin() {
        //Given
        Long groupId = 1L;
        given(userRepository.findById(any())).willReturn(Optional.of(user));

        //When
        groupLikeService.deleteSameGenderGroupLike(groupId, user.getId());

        //Then
        then(groupLikeToJoinRepository).should().deleteByFromUser_IdAndToGroup_Id(any(), any());
        then(groupLikeToJoinRepository).shouldHaveNoMoreInteractions();
    }

}