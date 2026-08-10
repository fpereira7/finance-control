package com.financecontrol.service;

import com.financecontrol.dto.CreditCardImportResponse;
import com.financecontrol.entity.ImportBatch;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.CsvImportException;
import com.financecontrol.mapper.CreditCardImportMapper;
import com.financecontrol.repository.ImportBatchRepository;
import java.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CreditCardImportService {

	private final ImportBatchRepository importBatchRepository;
	private final CreditCardStatementCsvParser csvParser;
	private final CreditCardImportMapper creditCardImportMapper;

	public CreditCardImportService(
			ImportBatchRepository importBatchRepository,
			CreditCardStatementCsvParser csvParser,
			CreditCardImportMapper creditCardImportMapper) {
		this.importBatchRepository = importBatchRepository;
		this.csvParser = csvParser;
		this.creditCardImportMapper = creditCardImportMapper;
	}

	@Transactional
	public CreditCardImportResponse importCsv(MultipartFile file) {
		validateFile(file);

		String fileName = file.getOriginalFilename();
		if (importBatchRepository.existsByFileNameIgnoreCase(fileName)) {
			throw new BusinessException("File already imported: " + fileName);
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
		batch.setRowCount(parseResult.lines().size());
		batch.setSkippedCount(parseResult.skippedCount());

		parseResult.lines().stream()
				.map(creditCardImportMapper::toEntity)
				.forEach(batch::addTransaction);

		ImportBatch saved = importBatchRepository.save(batch);
		return creditCardImportMapper.toResponse(saved);
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
}
