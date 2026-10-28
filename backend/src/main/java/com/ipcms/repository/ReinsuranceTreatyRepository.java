package com.ipcms.repository;

import com.ipcms.model.ReinsuranceTreaty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReinsuranceTreatyRepository extends JpaRepository<ReinsuranceTreaty, Long> {
    List<ReinsuranceTreaty> findByActiveYear(Integer activeYear);
}
