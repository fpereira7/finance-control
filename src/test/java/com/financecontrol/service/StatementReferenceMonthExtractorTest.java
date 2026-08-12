package com.financecontrol.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.financecontrol.exception.CsvImportException;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class StatementReferenceMonthExtractorTest {

	@Test
	void shouldExtractYearMonthFromFileName() {
		YearMonth result = StatementReferenceMonthExtractor.extractFromFileName("bradesco_2026-07-15.csv");
		assertEquals(YearMonth.of(2026, 7), result);
	}

	@Test
	void shouldExtractYearMonthWithoutDay() {
		YearMonth result = StatementReferenceMonthExtractor.extractFromFileName("nubank_2026-08.csv");
		assertEquals(YearMonth.of(2026, 8), result);
	}

	@Test
	void shouldRejectMissingReferenceMonth() {
		assertThrows(
				CsvImportException.class,
				() -> StatementReferenceMonthExtractor.extractFromFileName("fatura.csv"));
	}
}
