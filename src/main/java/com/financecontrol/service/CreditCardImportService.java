package com.financecontrol.service;

import com.financecontrol.dto.CreditCardImportDetailResponse;
import com.financecontrol.dto.CreditCardImportResponse;
import com.financecontrol.dto.CreditCardImportSummaryResponse;
import com.financecontrol.entity.CreditCardTransaction;
import com.financecontrol.entity.ImportBatch;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.CsvImportException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.mapper.CreditCardImportMapper;
import com.financecontrol.repository.CreditCardTransactionRepository;
import com.financecontrol.repository.ImportBatchRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CreditCardImportService {

	private final ImportBatchRepository importBatchRepository;
	private final CreditCardTransactionRepository creditCardTransactionRepository;
	private final CreditCardStatementCsvParser csvParser;
	private final CreditCardImportMapper creditCardImportMapper;

	public CreditCardImportService(
			ImportBatchRepository importBatchRepository,
			CreditCardTransactionRepository creditCardTransactionRepository,
			CreditCardStatementCsvParser csvParser,
			CreditCardImportMapper creditCardImportMapper) {
		this.importBatchRepository = importBatchRepository;
		this.creditCardTransactionRepository = creditCardTransactionRepository;
		this.csvParser = csvParser;
		this.creditCardImportMapper = creditCardImportMapper;
	}

	@Transactional
	public CreditCardImportResponse importCsv(MultipartFile file) {
		validateFile(file);

		String fileName = file.getOriginalFilename();
		YearMonth referenceMonth = StatementReferenceMonthExtractor.extractFromFileName(fileName);

		if (importBatchRepository.existsByFileNameIgnoreCase(fileName)) {
			throw new BusinessException("File already imported: " + fileName);
		}

		if (importBatchRepository.existsByReferenceYearAndReferenceMonth(
				referenceMonth.getYear(),
				referenceMonth.getMonthValue())) {
			throw new BusinessException(
					"A credit card statement was already imported for "
							+ referenceMonth
							+ ". Use a different reference month in the file name.");
		}

		CreditCardStatementParseResult parseResult;
		try {
			parseResult = csvParser.parse(file.getInputStream());
		}
		catch (IOException ex) {
			throw new CsvImportException("Failed to read uploaded file", ex);
		}

		ImportBatch batch = new ImportBatch();
		batch.setFileName(fileName);
		batch.setReferenceYear(referenceMonth.getYear());
		batch.setReferenceMonth(referenceMonth.getMonthValue());
		batch.setRowCount(parseResult.lines().size());
		batch.setSkippedCount(parseResult.skippedCount());

		parseResult.lines().stream()
				.map(creditCardImportMapper::toEntity)
				.forEach(batch::addTransaction);

		ImportBatch saved = importBatchRepository.save(batch);
		return creditCardImportMapper.toResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<CreditCardImportSummaryResponse> findByMonth(int year, int month) {
		validateYearMonth(year, month);

		return importBatchRepository
				.findByReferenceYearAndReferenceMonthOrderByImportedAtDesc(year, month)
				.stream()
				.map(batch -> creditCardImportMapper.toSummary(
						batch,
						creditCardTransactionRepository.sumAmountByImportBatchId(batch.getId())))
				.toList();
	}

	@Transactional(readOnly = true)
	public CreditCardImportDetailResponse findById(Long id) {
		ImportBatch batch = importBatchRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Credit card import not found: " + id));

		List<CreditCardTransaction> transactions = creditCardTransactionRepository
				.findByImportBatchIdOrderByTransactionDateDescIdDesc(id);
		BigDecimal totalAmount = creditCardTransactionRepository.sumAmountByImportBatchId(id);

		return creditCardImportMapper.toDetail(batch, totalAmount, transactions);
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new CsvImportException("CSV file is required");
		}

		String fileName = file.getOriginalFilename();
		if (fileName == null || fileName.isBlank()) {
			throw new CsvImportException("CSV file name is required");
		}

		if (!fileName.toLowerCase().endsWith(".csv")) {
			throw new CsvImportException("Only .csv files are supported");
		}
	}

	private void validateYearMonth(int year, int month) {
		if (year < 1900 || year > 2100) {
			throw new IllegalArgumentException("year must be between 1900 and 2100");
		}
		if (month < 1 || month > 12) {
			throw new IllegalArgumentException("month must be between 1 and 12");
		}
	}
}
