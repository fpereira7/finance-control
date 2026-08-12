package com.financecontrol.service;

import com.financecontrol.dto.MonthlySummaryResponse;
import com.financecontrol.entity.PaymentStatus;
import com.financecontrol.entity.SalaryStatus;
import com.financecontrol.repository.CreditCardTransactionRepository;
import com.financecontrol.repository.MonthlyExpenseRepository;
import com.financecontrol.repository.SalaryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonthlySummaryService {

	private final SalaryRepository salaryRepository;
	private final MonthlyExpenseRepository monthlyExpenseRepository;
	private final CreditCardTransactionRepository creditCardTransactionRepository;

	public MonthlySummaryService(
			SalaryRepository salaryRepository,
			MonthlyExpenseRepository monthlyExpenseRepository,
			CreditCardTransactionRepository creditCardTransactionRepository) {
		this.salaryRepository = salaryRepository;
		this.monthlyExpenseRepository = monthlyExpenseRepository;
		this.creditCardTransactionRepository = creditCardTransactionRepository;
	}

	@Transactional(readOnly = true)
	public MonthlySummaryResponse getSummary(int year, int month) {
		validateYearMonth(year, month);

		LocalDate startDate = LocalDate.of(year, month, 1);
		LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

		BigDecimal salariesTotal = salaryRepository.sumAmountByPaymentDateBetween(startDate, endDate);
		BigDecimal salariesReceived = salaryRepository.sumAmountByPaymentDateBetweenAndStatus(
				startDate, endDate, SalaryStatus.RECEIVED);
		BigDecimal salariesPending = salaryRepository.sumAmountByPaymentDateBetweenAndStatus(
				startDate, endDate, SalaryStatus.PENDING);
		long salariesReceivedCount = salaryRepository.countByPaymentDateBetweenAndStatus(
				startDate, endDate, SalaryStatus.RECEIVED);
		long salariesPendingCount = salaryRepository.countByPaymentDateBetweenAndStatus(
				startDate, endDate, SalaryStatus.PENDING);

		BigDecimal fixedExpensesTotal = monthlyExpenseRepository.sumAmountByDueDateBetween(startDate, endDate);
		BigDecimal fixedExpensesPaid = monthlyExpenseRepository.sumAmountByDueDateBetweenAndPaymentStatus(
				startDate, endDate, PaymentStatus.PAID);
		BigDecimal fixedExpensesPending = monthlyExpenseRepository.sumAmountByDueDateBetweenAndPaymentStatus(
				startDate, endDate, PaymentStatus.PENDING);
		long fixedExpensesPaidCount = monthlyExpenseRepository.countByDueDateBetweenAndPaymentStatus(
				startDate, endDate, PaymentStatus.PAID);
		long fixedExpensesPendingCount = monthlyExpenseRepository.countByDueDateBetweenAndPaymentStatus(
				startDate, endDate, PaymentStatus.PENDING);

		BigDecimal creditCardTotal = creditCardTransactionRepository
				.sumAmountByReferenceYearAndReferenceMonth(year, month);
		long creditCardTransactionCount = creditCardTransactionRepository
				.countByReferenceYearAndReferenceMonth(year, month);

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
