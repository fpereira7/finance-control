package com.financecontrol.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreditCardStatementLine(
		LocalDate date,
		String title,
		BigDecimal amount
) {
}
