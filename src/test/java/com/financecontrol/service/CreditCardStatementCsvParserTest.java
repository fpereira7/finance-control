package com.financecontrol.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.financecontrol.exception.CsvImportException;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CreditCardStatementCsvParserTest {

	private final CreditCardStatementCsvParser parser = new CreditCardStatementCsvParser();

	@Test
	void shouldParseValidRowsAndSkipNegatives() {
		String csv = """
				date,title,amount
				2026-08-06,Ifd*Padaria Cheiro de,"27,58"
				2026-07-08,Pagamento recebido,"- 579,28"
				2026-07-08,Am Energia Online - Parcela 8/12,"23,67"
				""";

		CreditCardStatementParseResult result = parser.parse(toStream(csv));

		assertEquals(2, result.lines().size());
		assertEquals(1, result.skippedCount());
		assertEquals(LocalDate.of(2026, 8, 6), result.lines().get(0).date());
		assertEquals("Ifd*Padaria Cheiro de", result.lines().get(0).title());
		assertEquals(new BigDecimal("27.58"), result.lines().get(0).amount());
		assertEquals(new BigDecimal("23.67"), result.lines().get(1).amount());
	}

	@Test
	void shouldFailWhenHeaderIsInvalid() {
		String csv = """
				data,descricao,valor
				2026-08-06,Teste,"10,00"
				""";

		CsvImportException ex = assertThrows(CsvImportException.class, () -> parser.parse(toStream(csv)));
		assertTrue(ex.getMessage().contains("date"));
	}

	@Test
	void shouldFailWhenDateIsInvalid() {
		String csv = """
				date,title,amount
				06/08/2026,Teste,"10,00"
				""";

		CsvImportException ex = assertThrows(CsvImportException.class, () -> parser.parse(toStream(csv)));
		assertTrue(ex.getMessage().contains("invalid date"));
	}

	@Test
	void shouldFailWhenOnlyNegativesRemain() {
		String csv = """
				date,title,amount
				2026-07-08,Pagamento recebido,"- 579,28"
				""";

		CsvImportException ex = assertThrows(CsvImportException.class, () -> parser.parse(toStream(csv)));
		assertTrue(ex.getMessage().contains("no importable transactions"));
	}

	private ByteArrayInputStream toStream(String csv) {
		return new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
	}
}
