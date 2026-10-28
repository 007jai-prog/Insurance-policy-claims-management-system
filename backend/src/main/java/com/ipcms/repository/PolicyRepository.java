package com.ipcms.repository;

import com.ipcms.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByPolicyNumber(String policyNumber);
    List<Policy> findByPolicyholderId(Long policyholderId);
    List<Policy> findByStatus(String status);
    List<Policy> findByAssetIdentifier(String assetIdentifier);
    List<Policy> findByExpiryDateBetween(LocalDate start, LocalDate end);
}
