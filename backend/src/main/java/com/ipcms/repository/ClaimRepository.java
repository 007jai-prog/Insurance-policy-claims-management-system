package com.ipcms.repository;

import com.ipcms.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    Optional<Claim> findByClaimNumber(String claimNumber);
    List<Claim> findByPolicyId(Long policyId);
    List<Claim> findByReporterId(Long reporterId);
    List<Claim> findByStatus(String status);
    List<Claim> findByIsPaymentFrozenTrue();
    List<Claim> findByPolicy_AssetIdentifier(String assetIdentifier);
}
