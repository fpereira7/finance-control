package com.financecontrol.dto;

import java.math.BigDecimal;

public record MonthlySummaryResponse(
		int year,
		int month,
		BigDecimal salariesTotal,
		BigDecimal salariesReceived,
		BigDecimal salariesPending,
		long salariesReceivedCount,
		long salariesPendingCount,
		BigDecimal fixedExpensesTotal,
		BigDecimal fixedExpensesPaid,
		BigDecimal fixedExpensesPending,
		long fixedExpensesPaidCount,
		long fixedExpensesPendingCount,
		BigDecimal creditCardTotal,
		long creditCardTransactionCount,
		BigDecimal balance
) {
}
