package com.ting.ting.repository;

import com.ting.ting.domain.BlindRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlindRequestRepository extends JpaRepository<BlindRequest, Long> {
    @Override
    Optional<BlindRequest> findById(Long aLong);
}
