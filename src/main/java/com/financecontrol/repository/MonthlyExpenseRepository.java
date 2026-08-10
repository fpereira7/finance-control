package com.financecontrol.repository;

import com.financecontrol.entity.ExpenseCategory;
import com.financecontrol.entity.MonthlyExpense;
import com.financecontrol.entity.PaymentStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MonthlyExpenseRepository
		extends JpaRepository<MonthlyExpense, Long>, JpaSpecificationExecutor<MonthlyExpense> {

	static Specification<MonthlyExpense> withFilters(
			ExpenseCategory category,
			PaymentStatus paymentStatus,
			Integer year,
			Integer month) {
		return (root, query, builder) -> {
			List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

			if (category != null) {
				predicates.add(builder.equal(root.get("category"), category));
			}
			if (paymentStatus != null) {
				predicates.add(builder.equal(root.get("paymentStatus"), paymentStatus));
			}
			if (year != null && month != null) {
				LocalDate start = LocalDate.of(year, month, 1);
				LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
				predicates.add(builder.between(root.get("dueDate"), start, end));
			}
			else if (year != null) {
				LocalDate start = LocalDate.of(year, 1, 1);
				LocalDate end = LocalDate.of(year, 12, 31);
				predicates.add(builder.between(root.get("dueDate"), start, end));
			}

			query.orderBy(builder.asc(root.get("dueDate")), builder.asc(root.get("id")));
			return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
		};
	}
}
