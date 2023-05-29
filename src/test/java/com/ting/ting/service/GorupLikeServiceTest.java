package com.ting.ting.service;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupMember;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

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
    @Mock private GroupLikeToJoinRepository groupLikeToJoinRepository;
    @Mock private GroupLikeToDateRepository groupLikeToDateRepository;

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
        groupLikeService.createOppositeGenderGroupLike(fromGroupId, toGroupId, user.getId());

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
        groupLikeService.deleteOppositeGenderGroupLike(fromGroupId, user.getId(), toGroupId);

        //Then
        then(groupLikeToDateRepository).should().deleteByFromGroupMemberAndToGroup(any(), any());
    }

}