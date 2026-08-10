package com.financecontrol.dto;

import com.financecontrol.entity.ExpenseCategory;
import com.financecontrol.entity.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlyExpenseRequest(
		@NotNull(message = "category is required")
		ExpenseCategory category,

		@NotNull(message = "amount is required")
		@DecimalMin(value = "0.01", message = "amount must be greater than zero")
		BigDecimal amount,

		@NotNull(message = "dueDate is required")
		LocalDate dueDate,

		@NotNull(message = "paymentStatus is required")
		PaymentStatus paymentStatus
) {
}
