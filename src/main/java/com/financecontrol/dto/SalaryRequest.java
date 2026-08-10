package com.financecontrol.dto;

import com.financecontrol.entity.SalaryStatus;
import com.financecontrol.entity.SalaryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SalaryRequest(
		@NotNull(message = "type is required")
		SalaryType type,

		@Size(max = 255, message = "description must be at most 255 characters")
		String description,

		@NotNull(message = "amount is required")
		@DecimalMin(value = "0.01", message = "amount must be greater than zero")
		BigDecimal amount,

		@NotNull(message = "paymentDate is required")
		LocalDate paymentDate,

		@NotNull(message = "status is required")
		SalaryStatus status
) {
}
