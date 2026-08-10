package com.financecontrol.dto;

import java.time.Instant;

public record CreditCardImportResponse(
		Long importBatchId,
		String fileName,
		Instant importedAt,
		int rowsImported,
		int rowsSkipped
) {
}
