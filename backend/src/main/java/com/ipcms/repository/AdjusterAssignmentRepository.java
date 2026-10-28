package com.ipcms.repository;

import com.ipcms.model.AdjusterAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdjusterAssignmentRepository extends JpaRepository<AdjusterAssignment, Long> {
    List<AdjusterAssignment> findByClaimId(Long claimId);
    List<AdjusterAssignment> findBySpecialty(String specialty);
}
