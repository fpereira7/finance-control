package com.financecontrol.service;

import com.financecontrol.exception.CsvImportException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class CreditCardStatementCsvParser {

	private static final String HEADER_DATE = "date";
	private static final String HEADER_TITLE = "title";
	private static final String HEADER_AMOUNT = "amount";

	public CreditCardStatementParseResult parse(InputStream inputStream) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
				CSVParser csvParser = CSVFormat.DEFAULT.builder()
						.setHeader()
						.setSkipHeaderRecord(true)
						.setIgnoreEmptyLines(true)
						.setTrim(true)
						.build()
						.parse(reader)) {

			validateHeaders(csvParser);

			List<CreditCardStatementLine> lines = new ArrayList<>();
			int skippedCount = 0;
			long recordNumber = 1;

			for (CSVRecord record : csvParser) {
				recordNumber = record.getRecordNumber();
				ParsedAmount parsedAmount = parseAmount(getRequired(record, HEADER_AMOUNT, recordNumber), recordNumber);

				if (parsedAmount.negativeOrZero()) {
					skippedCount++;
					continue;
				}

				LocalDate date = parseDate(getRequired(record, HEADER_DATE, recordNumber), recordNumber);
				String title = getRequired(record, HEADER_TITLE, recordNumber);

				lines.add(new CreditCardStatementLine(date, title, parsedAmount.value()));
			}

			if (lines.isEmpty()) {
				throw new CsvImportException("CSV has no importable transactions after skipping negative amounts");
			}

			return new CreditCardStatementParseResult(lines, skippedCount);
		}
		catch (CsvImportException ex) {
			throw ex;
		}
		catch (IOException ex) {
			throw new CsvImportException("Failed to read CSV file", ex);
		}
		catch (IllegalArgumentException ex) {
			throw new CsvImportException("Invalid CSV format: " + ex.getMessage(), ex);
		}
	}

	private void validateHeaders(CSVParser csvParser) {
		var headerMap = csvParser.getHeaderMap();
		if (headerMap == null || headerMap.isEmpty()) {
			throw new CsvImportException("CSV header is missing. Expected columns: date,title,amount");
		}

		requireHeader(headerMap.keySet(), HEADER_DATE);
		requireHeader(headerMap.keySet(), HEADER_TITLE);
		requireHeader(headerMap.keySet(), HEADER_AMOUNT);
	}

	private void requireHeader(Iterable<String> headers, String expected) {
		for (String header : headers) {
			if (expected.equalsIgnoreCase(header)) {
				return;
			}
		}
		throw new CsvImportException("CSV header is missing required column: " + expected);
	}

	private String getRequired(CSVRecord record, String column, long recordNumber) {
		String value;
		try {
			value = record.get(column);
		}
		catch (IllegalArgumentException ex) {
			try {
				value = record.get(column.toLowerCase(Locale.ROOT));
			}
			catch (IllegalArgumentException ignored) {
				throw new CsvImportException(
						"Line " + recordNumber + ": missing column '" + column + "'");
			}
		}

		if (value == null || value.isBlank()) {
			throw new CsvImportException("Line " + recordNumber + ": column '" + column + "' is required");
		}
		return value.trim();
	}

	private LocalDate parseDate(String rawDate, long recordNumber) {
		try {
			return LocalDate.parse(rawDate);
		}
		catch (DateTimeParseException ex) {
			throw new CsvImportException(
					"Line " + recordNumber + ": invalid date '" + rawDate + "'. Expected format: yyyy-MM-dd",
					ex);
		}
	}

	private ParsedAmount parseAmount(String rawAmount, long recordNumber) {
		String normalized = rawAmount
				.replace("\"", "")
				.replace(" ", "")
				.replace(".", "")
				.replace(',', '.');

		try {
			BigDecimal value = new BigDecimal(normalized);
			return new ParsedAmount(value, value.signum() <= 0);
		}
		catch (NumberFormatException ex) {
			throw new CsvImportException(
					"Line " + recordNumber + ": invalid amount '" + rawAmount + "'",
					ex);
		}
	}

	private record ParsedAmount(BigDecimal value, boolean negativeOrZero) {
	}
}
