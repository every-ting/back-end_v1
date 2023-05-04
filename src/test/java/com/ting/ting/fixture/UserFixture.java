package com.ting.ting.fixture;

import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

public class UserFixture {

    public static User entity(Long userId) {
        User user = User.of("username", "email", "단국대학교", "통계학과", Gender.W, LocalDate.now());
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
