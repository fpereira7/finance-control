package com.financecontrol.service;

import java.util.List;

public record CreditCardStatementParseResult(
		List<CreditCardStatementLine> lines,
		int skippedCount
) {
}
