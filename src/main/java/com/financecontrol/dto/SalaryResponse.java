package com.financecontrol.dto;

import com.financecontrol.entity.SalaryStatus;
import com.financecontrol.entity.SalaryType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record SalaryResponse(
		Long id,
		SalaryType type,
		String description,
		BigDecimal amount,
		LocalDate paymentDate,
		SalaryStatus status,
		Instant createdAt,
		Instant updatedAt
) {
}
