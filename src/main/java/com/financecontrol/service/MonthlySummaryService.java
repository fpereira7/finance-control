package com.financecontrol.service;

import com.financecontrol.dto.MonthlySummaryResponse;
import com.financecontrol.entity.PaymentStatus;
import com.financecontrol.entity.SalaryStatus;
import com.financecontrol.repository.CreditCardTransactionRepository;
import com.financecontrol.repository.MonthlyExpenseRepository;
import com.financecontrol.repository.SalaryRepository;
import com.financecontrol.security.AuthenticatedUserAccessor;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonthlySummaryService {

	private final SalaryRepository salaryRepository;
	private final MonthlyExpenseRepository monthlyExpenseRepository;
	private final CreditCardTransactionRepository creditCardTransactionRepository;
	private final AuthenticatedUserAccessor authenticatedUserAccessor;

	public MonthlySummaryService(
			SalaryRepository salaryRepository,
			MonthlyExpenseRepository monthlyExpenseRepository,
			CreditCardTransactionRepository creditCardTransactionRepository,
			AuthenticatedUserAccessor authenticatedUserAccessor) {
		this.salaryRepository = salaryRepository;
		this.monthlyExpenseRepository = monthlyExpenseRepository;
		this.creditCardTransactionRepository = creditCardTransactionRepository;
		this.authenticatedUserAccessor = authenticatedUserAccessor;
	}

	@Transactional(readOnly = true)
	public MonthlySummaryResponse getSummary(int year, int month) {
		validateYearMonth(year, month);
		Long userId = authenticatedUserAccessor.requireUserId();

		LocalDate startDate = LocalDate.of(year, month, 1);
		LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

		BigDecimal salariesTotal = salaryRepository
				.sumAmountByUserIdAndPaymentDateBetween(userId, startDate, endDate);
		BigDecimal salariesReceived = salaryRepository.sumAmountByUserIdAndPaymentDateBetweenAndStatus(
				userId, startDate, endDate, SalaryStatus.RECEIVED);
		BigDecimal salariesPending = salaryRepository.sumAmountByUserIdAndPaymentDateBetweenAndStatus(
				userId, startDate, endDate, SalaryStatus.PENDING);
		long salariesReceivedCount = salaryRepository.countByUserIdAndPaymentDateBetweenAndStatus(
				userId, startDate, endDate, SalaryStatus.RECEIVED);
		long salariesPendingCount = salaryRepository.countByUserIdAndPaymentDateBetweenAndStatus(
				userId, startDate, endDate, SalaryStatus.PENDING);

		BigDecimal fixedExpensesTotal = monthlyExpenseRepository
				.sumAmountByUserIdAndDueDateBetween(userId, startDate, endDate);
		BigDecimal fixedExpensesPaid = monthlyExpenseRepository.sumAmountByUserIdAndDueDateBetweenAndPaymentStatus(
				userId, startDate, endDate, PaymentStatus.PAID);
		BigDecimal fixedExpensesPending = monthlyExpenseRepository.sumAmountByUserIdAndDueDateBetweenAndPaymentStatus(
				userId, startDate, endDate, PaymentStatus.PENDING);
		long fixedExpensesPaidCount = monthlyExpenseRepository.countByUserIdAndDueDateBetweenAndPaymentStatus(
				userId, startDate, endDate, PaymentStatus.PAID);
		long fixedExpensesPendingCount = monthlyExpenseRepository.countByUserIdAndDueDateBetweenAndPaymentStatus(
				userId, startDate, endDate, PaymentStatus.PENDING);

		BigDecimal creditCardTotal = creditCardTransactionRepository
				.sumAmountByUserIdAndReferenceYearAndReferenceMonth(userId, year, month);
		long creditCardTransactionCount = creditCardTransactionRepository
				.countByUserIdAndReferenceYearAndReferenceMonth(userId, year, month);

		BigDecimal balance = salariesTotal
				.subtract(fixedExpensesTotal)
				.subtract(creditCardTotal);

		return new MonthlySummaryResponse(
				year,
				month,
				salariesTotal,
				salariesReceived,
				salariesPending,
				salariesReceivedCount,
				salariesPendingCount,
				fixedExpensesTotal,
				fixedExpensesPaid,
				fixedExpensesPending,
				fixedExpensesPaidCount,
				fixedExpensesPendingCount,
				creditCardTotal,
				creditCardTransactionCount,
				balance
		);
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
