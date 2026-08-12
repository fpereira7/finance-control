package com.financecontrol.repository;

import com.financecontrol.entity.CreditCardTransaction;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreditCardTransactionRepository extends JpaRepository<CreditCardTransaction, Long> {

	@Query("""
			select coalesce(sum(t.amount), 0)
			from CreditCardTransaction t
			where t.importBatch.userId = :userId
			  and t.importBatch.referenceYear = :year
			  and t.importBatch.referenceMonth = :month
			""")
	BigDecimal sumAmountByUserIdAndReferenceYearAndReferenceMonth(
			@Param("userId") Long userId,
			@Param("year") int year,
			@Param("month") int month);

	@Query("""
			select count(t)
			from CreditCardTransaction t
			where t.importBatch.userId = :userId
			  and t.importBatch.referenceYear = :year
			  and t.importBatch.referenceMonth = :month
			""")
	long countByUserIdAndReferenceYearAndReferenceMonth(
			@Param("userId") Long userId,
			@Param("year") int year,
			@Param("month") int month);

	@Query("""
			select coalesce(sum(t.amount), 0)
			from CreditCardTransaction t
			where t.importBatch.id = :importBatchId
			""")
	BigDecimal sumAmountByImportBatchId(@Param("importBatchId") Long importBatchId);

	List<CreditCardTransaction> findByImportBatchIdOrderByTransactionDateDescIdDesc(Long importBatchId);
}
