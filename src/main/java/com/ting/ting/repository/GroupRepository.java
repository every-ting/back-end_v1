package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.custom.SuggestedGroupWithRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Page<Group> findAll(Pageable pageable);

    @Query(value = "select new com.ting.ting.domain.custom.SuggestedGroupWithRequestStatus(entity.id, entity.groupName, entity.gender, entity.school, entity.memberSizeLimit, entity.isMatched, entity.isJoinable, entity.memo, requests.status) " +
            "from Group entity left outer join GroupMemberRequest requests on requests.group = entity and requests.user = :user " +
            "where entity not in (select gm.group from GroupMember gm where gm.member = :user) and entity.gender = :gender and entity.isJoinable = true and entity.isMatched = false")
    Page<SuggestedGroupWithRequestStatus> findAllSuggestedGroupWithRequestStatusByUserAndGender(@Param("user") User user, @Param("gender") Gender gender, Pageable pageable);

    Optional<Group>findByGroupName(String name);
}
