package com.financecontrol.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.financecontrol.dto.MonthlySummaryResponse;
import com.financecontrol.entity.PaymentStatus;
import com.financecontrol.entity.SalaryStatus;
import com.financecontrol.repository.CreditCardTransactionRepository;
import com.financecontrol.repository.MonthlyExpenseRepository;
import com.financecontrol.repository.SalaryRepository;
import com.financecontrol.security.AuthenticatedUserAccessor;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonthlySummaryServiceTest {

	private static final Long USER_ID = 7L;

	@Mock
	private SalaryRepository salaryRepository;

	@Mock
	private MonthlyExpenseRepository monthlyExpenseRepository;

	@Mock
	private CreditCardTransactionRepository creditCardTransactionRepository;

	@Mock
	private AuthenticatedUserAccessor authenticatedUserAccessor;

	@InjectMocks
	private MonthlySummaryService monthlySummaryService;

	@BeforeEach
	void setUpUser() {
		lenient().when(authenticatedUserAccessor.requireUserId()).thenReturn(USER_ID);
	}

	@Test
	void shouldBuildDetailedMonthlySummaryForCurrentUser() {
		LocalDate start = LocalDate.of(2026, 8, 1);
		LocalDate end = LocalDate.of(2026, 8, 31);

		when(salaryRepository.sumAmountByUserIdAndPaymentDateBetween(USER_ID, start, end))
				.thenReturn(new BigDecimal("8000.00"));
		when(salaryRepository.sumAmountByUserIdAndPaymentDateBetweenAndStatus(
						USER_ID, start, end, SalaryStatus.RECEIVED))
				.thenReturn(new BigDecimal("5000.00"));
		when(salaryRepository.sumAmountByUserIdAndPaymentDateBetweenAndStatus(
						USER_ID, start, end, SalaryStatus.PENDING))
				.thenReturn(new BigDecimal("3000.00"));
		when(salaryRepository.countByUserIdAndPaymentDateBetweenAndStatus(
						USER_ID, start, end, SalaryStatus.RECEIVED))
				.thenReturn(1L);
		when(salaryRepository.countByUserIdAndPaymentDateBetweenAndStatus(
						USER_ID, start, end, SalaryStatus.PENDING))
				.thenReturn(1L);

		when(monthlyExpenseRepository.sumAmountByUserIdAndDueDateBetween(USER_ID, start, end))
				.thenReturn(new BigDecimal("2500.00"));
		when(monthlyExpenseRepository.sumAmountByUserIdAndDueDateBetweenAndPaymentStatus(
						USER_ID, start, end, PaymentStatus.PAID))
				.thenReturn(new BigDecimal("1800.00"));
		when(monthlyExpenseRepository.sumAmountByUserIdAndDueDateBetweenAndPaymentStatus(
						USER_ID, start, end, PaymentStatus.PENDING))
				.thenReturn(new BigDecimal("700.00"));
		when(monthlyExpenseRepository.countByUserIdAndDueDateBetweenAndPaymentStatus(
						USER_ID, start, end, PaymentStatus.PAID))
				.thenReturn(3L);
		when(monthlyExpenseRepository.countByUserIdAndDueDateBetweenAndPaymentStatus(
						USER_ID, start, end, PaymentStatus.PENDING))
				.thenReturn(2L);

		when(creditCardTransactionRepository.sumAmountByUserIdAndReferenceYearAndReferenceMonth(USER_ID, 2026, 8))
				.thenReturn(new BigDecimal("1200.00"));
		when(creditCardTransactionRepository.countByUserIdAndReferenceYearAndReferenceMonth(USER_ID, 2026, 8))
				.thenReturn(10L);

		MonthlySummaryResponse summary = monthlySummaryService.getSummary(2026, 8);

		assertEquals(2026, summary.year());
		assertEquals(8, summary.month());
		assertEquals(new BigDecimal("8000.00"), summary.salariesTotal());
		assertEquals(new BigDecimal("2500.00"), summary.fixedExpensesTotal());
		assertEquals(new BigDecimal("1200.00"), summary.creditCardTotal());
		assertEquals(new BigDecimal("4300.00"), summary.balance());
		assertEquals(10L, summary.creditCardTransactionCount());
	}

	@Test
	void shouldRejectInvalidMonth() {
		assertThrows(IllegalArgumentException.class, () -> monthlySummaryService.getSummary(2026, 13));
	}
}
