package com.financecontrol.mapper;

import com.financecontrol.dto.CreditCardImportDetailResponse;
import com.financecontrol.dto.CreditCardImportResponse;
import com.financecontrol.dto.CreditCardImportSummaryResponse;
import com.financecontrol.dto.CreditCardTransactionResponse;
import com.financecontrol.entity.CreditCardTransaction;
import com.financecontrol.entity.ImportBatch;
import com.financecontrol.service.CreditCardStatementLine;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CreditCardImportMapper {

	public CreditCardTransaction toEntity(CreditCardStatementLine line) {
		CreditCardTransaction transaction = new CreditCardTransaction();
		transaction.setTransactionDate(line.date());
		transaction.setTitle(line.title());
		transaction.setAmount(line.amount());
		return transaction;
	}

	public CreditCardImportResponse toResponse(ImportBatch batch) {
		return new CreditCardImportResponse(
				batch.getId(),
				batch.getFileName(),
				batch.getImportedAt(),
				batch.getReferenceYear(),
				batch.getReferenceMonth(),
				batch.getRowCount(),
				batch.getSkippedCount()
		);
	}

	public CreditCardImportSummaryResponse toSummary(ImportBatch batch, BigDecimal totalAmount) {
		return new CreditCardImportSummaryResponse(
				batch.getId(),
				batch.getFileName(),
				batch.getImportedAt(),
				batch.getReferenceYear(),
				batch.getReferenceMonth(),
				batch.getRowCount(),
				batch.getSkippedCount(),
				totalAmount
		);
	}

	public CreditCardTransactionResponse toTransactionResponse(CreditCardTransaction transaction) {
		return new CreditCardTransactionResponse(
				transaction.getId(),
				transaction.getTransactionDate(),
				transaction.getTitle(),
				transaction.getAmount()
		);
	}

	public CreditCardImportDetailResponse toDetail(
			ImportBatch batch,
			BigDecimal totalAmount,
			List<CreditCardTransaction> transactions) {
		List<CreditCardTransactionResponse> transactionResponses = transactions.stream()
				.map(this::toTransactionResponse)
				.toList();

		return new CreditCardImportDetailResponse(
				batch.getId(),
				batch.getFileName(),
				batch.getImportedAt(),
				batch.getReferenceYear(),
				batch.getReferenceMonth(),
				batch.getRowCount(),
				batch.getSkippedCount(),
				totalAmount,
				transactionResponses
		);
	}
}
