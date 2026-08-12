package com.financecontrol.repository;

import com.financecontrol.entity.ImportBatch;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {

	boolean existsByFileNameIgnoreCase(String fileName);

	boolean existsByReferenceYearAndReferenceMonth(int referenceYear, int referenceMonth);

	List<ImportBatch> findByReferenceYearAndReferenceMonthOrderByImportedAtDesc(
			int referenceYear,
			int referenceMonth);
}
