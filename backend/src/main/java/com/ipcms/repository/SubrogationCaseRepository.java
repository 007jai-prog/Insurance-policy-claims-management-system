package com.ipcms.repository;

import com.ipcms.model.SubrogationCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubrogationCaseRepository extends JpaRepository<SubrogationCase, Long> {
    List<SubrogationCase> findByClaimId(Long claimId);
    List<SubrogationCase> findByStatus(String status);
}
