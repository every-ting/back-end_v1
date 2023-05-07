package com.ting.ting.fixture;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.dto.request.GroupRequest;
import org.springframework.test.util.ReflectionTestUtils;

public class GroupFixture {

    public static Group entity(Long groupId) {
        Group entity = request().toEntity();
        ReflectionTestUtils.setField(entity, "id", groupId);
        return entity;
    }

    public static GroupRequest request() {
        GroupRequest request = new GroupRequest("팀", Gender.WOMEN, 5, "단국대학교", "");
        return request;
    }
}
