package com.financecontrol.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CreditCardImportSummaryResponse(
		Long importBatchId,
		String fileName,
		Instant importedAt,
		int referenceYear,
		int referenceMonth,
		int rowsImported,
		int rowsSkipped,
		BigDecimal totalAmount
) {
}
