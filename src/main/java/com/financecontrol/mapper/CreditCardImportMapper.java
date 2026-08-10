package com.financecontrol.mapper;

import com.financecontrol.dto.CreditCardImportResponse;
import com.financecontrol.entity.CreditCardTransaction;
import com.financecontrol.entity.ImportBatch;
import com.financecontrol.service.CreditCardStatementLine;
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
				batch.getRowCount(),
				batch.getSkippedCount()
		);
	}
}
