package com.financecontrol.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.financecontrol.dto.MonthlyExpenseRequest;
import com.financecontrol.dto.MonthlyExpenseResponse;
import com.financecontrol.entity.ExpenseCategory;
import com.financecontrol.entity.MonthlyExpense;
import com.financecontrol.entity.PaymentStatus;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.mapper.MonthlyExpenseMapper;
import com.financecontrol.repository.MonthlyExpenseRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class MonthlyExpenseServiceTest {

	@Mock
	private MonthlyExpenseRepository monthlyExpenseRepository;

	@Mock
	private MonthlyExpenseMapper monthlyExpenseMapper;

	@InjectMocks
	private MonthlyExpenseService monthlyExpenseService;

	@Test
	void createShouldPersistExpense() {
		MonthlyExpenseRequest request = new MonthlyExpenseRequest(
				ExpenseCategory.RENT,
				new BigDecimal("1500.00"),
				LocalDate.of(2026, 8, 10),
				PaymentStatus.PENDING);

		MonthlyExpense entity = new MonthlyExpense();
		MonthlyExpense saved = buildExpense(1L);
		MonthlyExpenseResponse response = buildResponse(1L);

		when(monthlyExpenseMapper.toEntity(request)).thenReturn(entity);
		when(monthlyExpenseRepository.save(entity)).thenReturn(saved);
		when(monthlyExpenseMapper.toResponse(saved)).thenReturn(response);

		MonthlyExpenseResponse result = monthlyExpenseService.create(request);

		assertEquals(1L, result.id());
		assertEquals(ExpenseCategory.RENT, result.category());
		verify(monthlyExpenseRepository).save(entity);
	}

	@Test
	void findByIdShouldFailWhenMissing() {
		when(monthlyExpenseRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> monthlyExpenseService.findById(99L));
	}

	@Test
	void findAllShouldRejectMonthWithoutYear() {
		assertThrows(
				IllegalArgumentException.class,
				() -> monthlyExpenseService.findAll(null, null, null, 8));
	}

	@Test
	@SuppressWarnings("unchecked")
	void findAllShouldApplyFilters() {
		MonthlyExpense expense = buildExpense(1L);
		MonthlyExpenseResponse response = buildResponse(1L);

		when(monthlyExpenseRepository.findAll(any(Specification.class))).thenReturn(List.of(expense));
		when(monthlyExpenseMapper.toResponse(expense)).thenReturn(response);

		List<MonthlyExpenseResponse> result = monthlyExpenseService.findAll(
				ExpenseCategory.ENERGY,
				PaymentStatus.PENDING,
				2026,
				8);

		assertEquals(1, result.size());
		assertEquals(ExpenseCategory.RENT, result.getFirst().category());
	}

	private MonthlyExpense buildExpense(Long id) {
		MonthlyExpense expense = new MonthlyExpense();
		expense.setId(id);
		expense.setCategory(ExpenseCategory.RENT);
		expense.setAmount(new BigDecimal("1500.00"));
		expense.setDueDate(LocalDate.of(2026, 8, 10));
		expense.setPaymentStatus(PaymentStatus.PENDING);
		expense.setCreatedAt(Instant.parse("2026-08-10T12:00:00Z"));
		expense.setUpdatedAt(Instant.parse("2026-08-10T12:00:00Z"));
		return expense;
	}

	private MonthlyExpenseResponse buildResponse(Long id) {
		return new MonthlyExpenseResponse(
				id,
				ExpenseCategory.RENT,
				new BigDecimal("1500.00"),
				LocalDate.of(2026, 8, 10),
				PaymentStatus.PENDING,
				Instant.parse("2026-08-10T12:00:00Z"),
				Instant.parse("2026-08-10T12:00:00Z"));
	}
}
