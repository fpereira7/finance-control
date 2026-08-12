package com.financecontrol.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.financecontrol.dto.CreditCardImportResponse;
import com.financecontrol.entity.ImportBatch;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.CsvImportException;
import com.financecontrol.mapper.CreditCardImportMapper;
import com.financecontrol.repository.CreditCardTransactionRepository;
import com.financecontrol.repository.ImportBatchRepository;
import com.financecontrol.security.AuthenticatedUserAccessor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class CreditCardImportServiceTest {

	private static final Long USER_ID = 7L;

	@Mock
	private ImportBatchRepository importBatchRepository;

	@Mock
	private CreditCardTransactionRepository creditCardTransactionRepository;

	@Mock
	private CreditCardStatementCsvParser csvParser;

	@Mock
	private CreditCardImportMapper creditCardImportMapper;

	@Mock
	private AuthenticatedUserAccessor authenticatedUserAccessor;

	@InjectMocks
	private CreditCardImportService creditCardImportService;

	@BeforeEach
	void setUpUser() {
		lenient().when(authenticatedUserAccessor.requireUserId()).thenReturn(USER_ID);
	}

	@Test
	void shouldRejectDuplicateFileName() {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"bradesco_2026-07-15.csv",
				"text/csv",
				"date,title,amount".getBytes());

		when(importBatchRepository.existsByUserIdAndFileNameIgnoreCase(USER_ID, "bradesco_2026-07-15.csv"))
				.thenReturn(true);

		assertThrows(BusinessException.class, () -> creditCardImportService.importCsv(file));
		verify(csvParser, never()).parse(any());
	}

	@Test
	void shouldRejectDuplicateReferenceMonth() {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"bradesco_2026-07-20.csv",
				"text/csv",
				"content".getBytes());

		when(importBatchRepository.existsByUserIdAndFileNameIgnoreCase(USER_ID, "bradesco_2026-07-20.csv"))
				.thenReturn(false);
		when(importBatchRepository.existsByUserIdAndReferenceYearAndReferenceMonth(USER_ID, 2026, 7))
				.thenReturn(true);

		assertThrows(BusinessException.class, () -> creditCardImportService.importCsv(file));
		verify(csvParser, never()).parse(any());
	}

	@Test
	void shouldRejectFileNameWithoutReferenceMonth() {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"fatura.csv",
				"text/csv",
				"content".getBytes());

		assertThrows(CsvImportException.class, () -> creditCardImportService.importCsv(file));
		verify(importBatchRepository, never()).existsByUserIdAndFileNameIgnoreCase(anyLong(), any());
	}

	@Test
	void shouldRejectEmptyFile() {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"bradesco_2026-07-15.csv",
				"text/csv",
				new byte[0]);

		assertThrows(CsvImportException.class, () -> creditCardImportService.importCsv(file));
	}

	@Test
	void shouldImportValidFileWithReferenceMonth() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"bradesco_2026-07-15.csv",
				"text/csv",
				"content".getBytes());

		CreditCardStatementLine line = new CreditCardStatementLine(
				LocalDate.of(2026, 6, 20),
				"Padaria",
				new BigDecimal("27.58"));
		CreditCardStatementParseResult parseResult = new CreditCardStatementParseResult(List.of(line), 1);

		ImportBatch saved = new ImportBatch();
		saved.setId(10L);
		saved.setUserId(USER_ID);
		saved.setFileName("bradesco_2026-07-15.csv");
		saved.setImportedAt(Instant.parse("2026-08-10T12:00:00Z"));
		saved.setReferenceYear(2026);
		saved.setReferenceMonth(7);
		saved.setRowCount(1);
		saved.setSkippedCount(1);

		CreditCardImportResponse response = new CreditCardImportResponse(
				10L,
				"bradesco_2026-07-15.csv",
				saved.getImportedAt(),
				2026,
				7,
				1,
				1);

		when(importBatchRepository.existsByUserIdAndFileNameIgnoreCase(USER_ID, "bradesco_2026-07-15.csv"))
				.thenReturn(false);
		when(importBatchRepository.existsByUserIdAndReferenceYearAndReferenceMonth(USER_ID, 2026, 7))
				.thenReturn(false);
		when(csvParser.parse(any())).thenReturn(parseResult);
		when(creditCardImportMapper.toEntity(line)).thenReturn(new com.financecontrol.entity.CreditCardTransaction());
		when(importBatchRepository.save(any(ImportBatch.class))).thenReturn(saved);
		when(creditCardImportMapper.toResponse(saved)).thenReturn(response);

		CreditCardImportResponse result = creditCardImportService.importCsv(file);

		ArgumentCaptor<ImportBatch> batchCaptor = ArgumentCaptor.forClass(ImportBatch.class);
		verify(importBatchRepository).save(batchCaptor.capture());
		assertEquals(USER_ID, batchCaptor.getValue().getUserId());
		assertEquals(2026, batchCaptor.getValue().getReferenceYear());
		assertEquals(7, batchCaptor.getValue().getReferenceMonth());
		assertEquals(10L, result.importBatchId());
		verify(creditCardTransactionRepository, never())
				.sumAmountByUserIdAndReferenceYearAndReferenceMonth(anyLong(), anyInt(), anyInt());
	}
}
