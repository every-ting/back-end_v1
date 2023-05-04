package com.ting.ting.fixture;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.dto.GroupDto;
import org.springframework.test.util.ReflectionTestUtils;

public class GroupFixture {

    public static Group entity(Long groupId) {
        User leader = UserFixture.entity(groupId + 1);
        Group entity = dto().toEntity(leader);
        ReflectionTestUtils.setField(leader, "id", 1L);
        return entity;
    }

    public static GroupDto dto() {
        GroupDto dto = GroupDto.of("팀", Gender.W, 5, "단국대학교", "");
        return dto;
    }
}
