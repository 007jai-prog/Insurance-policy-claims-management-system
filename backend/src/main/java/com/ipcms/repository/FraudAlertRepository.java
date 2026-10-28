package com.ipcms.repository;

import com.ipcms.model.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    List<FraudAlert> findByIsReviewedFalse();
    List<FraudAlert> findByClaimId(Long claimId);
}
