package com.ipcms.repository;

import com.ipcms.model.RenewalNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RenewalNoticeRepository extends JpaRepository<RenewalNotice, Long> {
    List<RenewalNotice> findByPolicyId(Long policyId);
    List<RenewalNotice> findByStatus(String status);
}
