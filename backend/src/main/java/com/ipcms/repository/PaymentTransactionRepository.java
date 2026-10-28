package com.ipcms.repository;

import com.ipcms.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByPolicyId(Long policyId);
    List<PaymentTransaction> findByPolicy_Policyholder_Id(Long policyholderId);
    List<PaymentTransaction> findByStatus(String status);
}
