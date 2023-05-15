package com.ting.ting.repository;

import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.domain.custom.BlindUserWithRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Override
    Optional<User> findById(Long id);

    Page<User> findAllByGenderAndIdNotIn(Gender gender, Set<Long> usersId, Pageable pageable);

    @Query(value = "select new com.ting.ting.domain.custom.BlindUserWithRequestStatus(entity.id, entity.username, entity.major, entity.mbti, entity.weight, entity.height, entity.idealPhoto, requests.status) " +
            "from User entity left outer join BlindRequest requests on requests.fromUser = :user and requests.toUser = entity" +
            " where entity not in (select fromUser from BlindRequest where toUser =: user) and entity.gender = :gender")
    Page<BlindUserWithRequestStatus> findAllBlindUserWithRequestStatusByUserAndGender(@Param("user") User user, @Param("gender") Gender gender, Pageable pageable);
}
