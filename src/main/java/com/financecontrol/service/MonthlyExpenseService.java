package com.financecontrol.service;

import com.financecontrol.dto.MonthlyExpenseRequest;
import com.financecontrol.dto.MonthlyExpenseResponse;
import com.financecontrol.entity.ExpenseCategory;
import com.financecontrol.entity.MonthlyExpense;
import com.financecontrol.entity.PaymentStatus;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.mapper.MonthlyExpenseMapper;
import com.financecontrol.repository.MonthlyExpenseRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonthlyExpenseService {

	private final MonthlyExpenseRepository monthlyExpenseRepository;
	private final MonthlyExpenseMapper monthlyExpenseMapper;

	public MonthlyExpenseService(
			MonthlyExpenseRepository monthlyExpenseRepository,
			MonthlyExpenseMapper monthlyExpenseMapper) {
		this.monthlyExpenseRepository = monthlyExpenseRepository;
		this.monthlyExpenseMapper = monthlyExpenseMapper;
	}

	@Transactional(readOnly = true)
	public List<MonthlyExpenseResponse> findAll(
			ExpenseCategory category,
			PaymentStatus paymentStatus,
			Integer year,
			Integer month) {
		validateYearMonth(year, month);

		return monthlyExpenseRepository
				.findAll(MonthlyExpenseRepository.withFilters(category, paymentStatus, year, month))
				.stream()
				.map(monthlyExpenseMapper::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public MonthlyExpenseResponse findById(Long id) {
		return monthlyExpenseMapper.toResponse(getOrThrow(id));
	}

	@Transactional
	public MonthlyExpenseResponse create(MonthlyExpenseRequest request) {
		MonthlyExpense expense = monthlyExpenseMapper.toEntity(request);
		MonthlyExpense saved = monthlyExpenseRepository.save(expense);
		return monthlyExpenseMapper.toResponse(saved);
	}

	@Transactional
	public MonthlyExpenseResponse update(Long id, MonthlyExpenseRequest request) {
		MonthlyExpense expense = getOrThrow(id);
		monthlyExpenseMapper.updateEntity(expense, request);
		return monthlyExpenseMapper.toResponse(expense);
	}

	@Transactional
	public void delete(Long id) {
		MonthlyExpense expense = getOrThrow(id);
		monthlyExpenseRepository.delete(expense);
	}

	private MonthlyExpense getOrThrow(Long id) {
		return monthlyExpenseRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Monthly expense not found: " + id));
	}

	private void validateYearMonth(Integer year, Integer month) {
		if (month != null && year == null) {
			throw new IllegalArgumentException("year is required when month is provided");
		}
		if (year != null && (year < 1900 || year > 2100)) {
			throw new IllegalArgumentException("year must be between 1900 and 2100");
		}
		if (month != null && (month < 1 || month > 12)) {
			throw new IllegalArgumentException("month must be between 1 and 12");
		}
	}
}
