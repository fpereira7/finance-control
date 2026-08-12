package com.financecontrol.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreditCardTransactionResponse(
		Long id,
		LocalDate transactionDate,
		String title,
		BigDecimal amount
) {
}
