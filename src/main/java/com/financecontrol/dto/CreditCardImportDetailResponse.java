package com.financecontrol.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CreditCardImportDetailResponse(
		Long importBatchId,
		String fileName,
		Instant importedAt,
		int referenceYear,
		int referenceMonth,
		int rowsImported,
		int rowsSkipped,
		BigDecimal totalAmount,
		List<CreditCardTransactionResponse> transactions
) {
}
