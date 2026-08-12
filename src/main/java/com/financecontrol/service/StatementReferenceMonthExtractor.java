package com.financecontrol.service;

import com.financecontrol.exception.CsvImportException;
import java.time.YearMonth;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StatementReferenceMonthExtractor {

	private static final Pattern YEAR_MONTH_PATTERN = Pattern.compile("(20\\d{2}|19\\d{2})-(\\d{2})(?:-\\d{2})?");

	private StatementReferenceMonthExtractor() {
	}

	public static YearMonth extractFromFileName(String fileName) {
		if (fileName == null || fileName.isBlank()) {
			throw new CsvImportException("CSV file name is required to determine the statement reference month");
		}

		Matcher matcher = YEAR_MONTH_PATTERN.matcher(fileName);
		if (!matcher.find()) {
			throw new CsvImportException(
					"Could not determine statement month from file name '" + fileName
							+ "'. Expected a date like yyyy-MM or yyyy-MM-dd in the file name "
							+ "(example: bradesco_2026-07-15.csv)");
		}

		int year = Integer.parseInt(matcher.group(1));
		int month = Integer.parseInt(matcher.group(2));

		try {
			return YearMonth.of(year, month);
		}
		catch (Exception ex) {
			throw new CsvImportException(
					"Invalid statement month extracted from file name '" + fileName + "'",
					ex);
		}
	}
}
