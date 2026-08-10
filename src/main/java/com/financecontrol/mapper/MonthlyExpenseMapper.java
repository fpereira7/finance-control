package com.financecontrol.mapper;

import com.financecontrol.dto.MonthlyExpenseRequest;
import com.financecontrol.dto.MonthlyExpenseResponse;
import com.financecontrol.entity.MonthlyExpense;
import org.springframework.stereotype.Component;

@Component
public class MonthlyExpenseMapper {

	public MonthlyExpense toEntity(MonthlyExpenseRequest request) {
		MonthlyExpense expense = new MonthlyExpense();
		apply(request, expense);
		return expense;
	}

	public void updateEntity(MonthlyExpense expense, MonthlyExpenseRequest request) {
		apply(request, expense);
	}

	public MonthlyExpenseResponse toResponse(MonthlyExpense expense) {
		return new MonthlyExpenseResponse(
				expense.getId(),
				expense.getCategory(),
				expense.getAmount(),
				expense.getDueDate(),
				expense.getPaymentStatus(),
				expense.getCreatedAt(),
				expense.getUpdatedAt()
		);
	}

	private void apply(MonthlyExpenseRequest request, MonthlyExpense expense) {
		expense.setCategory(request.category());
		expense.setAmount(request.amount());
		expense.setDueDate(request.dueDate());
		expense.setPaymentStatus(request.paymentStatus());
	}
}
