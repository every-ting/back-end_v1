package com.ting.ting.repository;

import com.ting.ting.domain.Group;
import com.ting.ting.domain.GroupDateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupDateRequestRepository extends JpaRepository<GroupDateRequest, Long> {

    boolean existsByFromGroupAndToGroup(Group fromGroup, Group toGroup);

    void deleteByFromGroupAndToGroup(Group fromGroup, Group toGroup);

    void deleteByFromGroup_IdAndToGroup_Id(Long fromGroupId, Long toGroupId);

    List<GroupDateRequest> findAllByFromGroup(Group fromGroup);

    Page<GroupDateRequest> findAllByFromGroup_IsMatchedAndToGroup(boolean isMatched, Group fromGroup, Pageable pageable);
}
