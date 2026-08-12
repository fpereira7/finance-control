package com.financecontrol.repository;

import com.financecontrol.entity.ImportBatch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {

	boolean existsByUserIdAndFileNameIgnoreCase(Long userId, String fileName);

	boolean existsByUserIdAndReferenceYearAndReferenceMonth(Long userId, int referenceYear, int referenceMonth);

	List<ImportBatch> findByUserIdAndReferenceYearAndReferenceMonthOrderByImportedAtDesc(
			Long userId,
			int referenceYear,
			int referenceMonth);

	Optional<ImportBatch> findByIdAndUserId(Long id, Long userId);
}
