package com.financecontrol.service;

import com.financecontrol.exception.CsvImportException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
		try (PushbackInputStream pushback = new PushbackInputStream(inputStream, 3);
				BufferedReader reader = new BufferedReader(new InputStreamReader(stripBom(pushback), StandardCharsets.UTF_8));
				CSVParser csvParser = CSVFormat.DEFAULT.builder()
						.setHeader()
						.setSkipHeaderRecord(true)
						.setIgnoreEmptyLines(true)
						.setTrim(true)
						.build()
						.parse(reader)) {

			Map<String, String> normalizedHeaders = buildNormalizedHeaderMap(csvParser);
			validateHeaders(normalizedHeaders);

			List<CreditCardStatementLine> lines = new ArrayList<>();
			int skippedCount = 0;
			long recordNumber = 1;

			for (CSVRecord record : csvParser) {
				recordNumber = record.getRecordNumber();
				ParsedAmount parsedAmount = parseAmount(
						getRequired(record, normalizedHeaders, HEADER_AMOUNT, recordNumber),
						recordNumber);

				if (parsedAmount.negativeOrZero()) {
					skippedCount++;
					continue;
				}

				LocalDate date = parseDate(
						getRequired(record, normalizedHeaders, HEADER_DATE, recordNumber),
						recordNumber);
				String title = getRequired(record, normalizedHeaders, HEADER_TITLE, recordNumber);

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

	private InputStream stripBom(PushbackInputStream inputStream) throws IOException {
		byte[] bom = new byte[3];
		int read = inputStream.read(bom);
		boolean hasUtf8Bom = read == 3
				&& (bom[0] & 0xFF) == 0xEF
				&& (bom[1] & 0xFF) == 0xBB
				&& (bom[2] & 0xFF) == 0xBF;

		if (!hasUtf8Bom && read > 0) {
			inputStream.unread(bom, 0, read);
		}

		return inputStream;
	}

	private Map<String, String> buildNormalizedHeaderMap(CSVParser csvParser) {
		Map<String, String> normalizedHeaders = new LinkedHashMap<>();
		for (String header : csvParser.getHeaderNames()) {
			normalizedHeaders.put(normalizeHeader(header), header);
		}
		return normalizedHeaders;
	}

	private void validateHeaders(Map<String, String> normalizedHeaders) {
		if (normalizedHeaders.isEmpty()) {
			throw new CsvImportException("CSV header is missing. Expected columns: date,title,amount");
		}

		requireHeader(normalizedHeaders, HEADER_DATE);
		requireHeader(normalizedHeaders, HEADER_TITLE);
		requireHeader(normalizedHeaders, HEADER_AMOUNT);
	}

	private void requireHeader(Map<String, String> normalizedHeaders, String expected) {
		if (!normalizedHeaders.containsKey(expected)) {
			throw new CsvImportException("CSV header is missing required column: " + expected);
		}
	}

	private String getRequired(
			CSVRecord record,
			Map<String, String> normalizedHeaders,
			String column,
			long recordNumber) {
		String actualHeader = normalizedHeaders.get(column);
		if (actualHeader == null) {
			throw new CsvImportException("Line " + recordNumber + ": missing column '" + column + "'");
		}

		String value;
		try {
			value = record.get(actualHeader);
		}
		catch (IllegalArgumentException ex) {
			throw new CsvImportException("Line " + recordNumber + ": missing column '" + column + "'", ex);
		}

		if (value == null || value.isBlank()) {
			throw new CsvImportException("Line " + recordNumber + ": column '" + column + "' is required");
		}
		return value.trim();
	}

	private String normalizeHeader(String header) {
		if (header == null) {
			return "";
		}
		return header.replace("\uFEFF", "").trim().toLowerCase(Locale.ROOT);
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
