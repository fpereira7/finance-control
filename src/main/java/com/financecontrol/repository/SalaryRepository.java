package com.financecontrol.repository;

import com.financecontrol.entity.Salary;
import com.financecontrol.entity.SalaryStatus;
import com.financecontrol.entity.SalaryType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalaryRepository extends JpaRepository<Salary, Long>, JpaSpecificationExecutor<Salary> {

	@Query("""
			select coalesce(sum(s.amount), 0)
			from Salary s
			where s.paymentDate between :startDate and :endDate
			""")
	BigDecimal sumAmountByPaymentDateBetween(
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	@Query("""
			select coalesce(sum(s.amount), 0)
			from Salary s
			where s.paymentDate between :startDate and :endDate
			  and s.status = :status
			""")
	BigDecimal sumAmountByPaymentDateBetweenAndStatus(
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate,
			@Param("status") SalaryStatus status);

	long countByPaymentDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, SalaryStatus status);

	static Specification<Salary> withFilters(
			SalaryType type,
			SalaryStatus status,
			Integer year,
			Integer month) {
		return (root, query, builder) -> {
			List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

			if (type != null) {
				predicates.add(builder.equal(root.get("type"), type));
			}
			if (status != null) {
				predicates.add(builder.equal(root.get("status"), status));
			}
			if (year != null && month != null) {
				LocalDate start = LocalDate.of(year, month, 1);
				LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
				predicates.add(builder.between(root.get("paymentDate"), start, end));
			}
			else if (year != null) {
				LocalDate start = LocalDate.of(year, 1, 1);
				LocalDate end = LocalDate.of(year, 12, 31);
				predicates.add(builder.between(root.get("paymentDate"), start, end));
			}

			query.orderBy(builder.asc(root.get("paymentDate")), builder.asc(root.get("id")));
			return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
		};
	}
}
