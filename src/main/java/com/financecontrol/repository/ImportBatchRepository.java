package com.financecontrol.repository;

import com.financecontrol.entity.ImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {

	boolean existsByFileNameIgnoreCase(String fileName);
}
