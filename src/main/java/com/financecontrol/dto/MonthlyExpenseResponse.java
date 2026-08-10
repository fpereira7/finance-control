package com.financecontrol.dto;

import com.financecontrol.entity.ExpenseCategory;
import com.financecontrol.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record MonthlyExpenseResponse(
		Long id,
		ExpenseCategory category,
		BigDecimal amount,
		LocalDate dueDate,
		PaymentStatus paymentStatus,
		Instant createdAt,
		Instant updatedAt
) {
}
