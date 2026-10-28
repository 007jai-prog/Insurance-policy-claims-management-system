package com.ipcms.repository;

import com.ipcms.model.Endorsement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EndorsementRepository extends JpaRepository<Endorsement, Long> {
    List<Endorsement> findByPolicyIdOrderByProcessedAtDesc(Long policyId);
}
