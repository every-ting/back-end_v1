package com.ting.ting.fixture;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.dto.request.GroupCreateRequest;
import org.springframework.test.util.ReflectionTestUtils;

public class GroupFixture {

    public static Group createGroupById(Long id) {
        Group entity = request().toEntity(Gender.WOMEN, "단국대학교");
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    public static GroupCreateRequest request() {
        GroupCreateRequest request = new GroupCreateRequest("팀", 5, "");
        return request;
    }
}
