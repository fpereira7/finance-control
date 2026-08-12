package com.financecontrol.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.financecontrol.dto.SalaryRequest;
import com.financecontrol.dto.SalaryResponse;
import com.financecontrol.entity.Salary;
import com.financecontrol.entity.SalaryStatus;
import com.financecontrol.entity.SalaryType;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.mapper.SalaryMapper;
import com.financecontrol.repository.SalaryRepository;
import com.financecontrol.security.AuthenticatedUserAccessor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class SalaryServiceTest {

	private static final Long USER_ID = 7L;

	@Mock
	private SalaryRepository salaryRepository;

	@Mock
	private SalaryMapper salaryMapper;

	@Mock
	private AuthenticatedUserAccessor authenticatedUserAccessor;

	@InjectMocks
	private SalaryService salaryService;

	@BeforeEach
	void setUpUser() {
		lenient().when(authenticatedUserAccessor.requireUserId()).thenReturn(USER_ID);
	}

	@Test
	void createShouldPersistSalaryForCurrentUser() {
		SalaryRequest request = new SalaryRequest(
				SalaryType.CLT,
				"Empresa X",
				new BigDecimal("5000.00"),
				LocalDate.of(2026, 8, 5),
				SalaryStatus.PENDING);

		Salary entity = new Salary();
		Salary saved = buildSalary(1L);
		SalaryResponse response = buildResponse(1L);

		when(salaryMapper.toEntity(request)).thenReturn(entity);
		when(salaryRepository.save(entity)).thenReturn(saved);
		when(salaryMapper.toResponse(saved)).thenReturn(response);

		SalaryResponse result = salaryService.create(request);

		ArgumentCaptor<Salary> captor = ArgumentCaptor.forClass(Salary.class);
		verify(salaryRepository).save(captor.capture());
		assertEquals(USER_ID, captor.getValue().getUserId());
		assertEquals(1L, result.id());
	}

	@Test
	void findByIdShouldFailWhenMissingForUser() {
		when(salaryRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> salaryService.findById(99L));
	}

	@Test
	void findAllShouldRejectMonthWithoutYear() {
		assertThrows(
				IllegalArgumentException.class,
				() -> salaryService.findAll(null, null, null, 8));
	}

	@Test
	@SuppressWarnings("unchecked")
	void findAllShouldApplyFilters() {
		Salary salary = buildSalary(1L);
		SalaryResponse response = buildResponse(1L);

		when(salaryRepository.findAll(any(Specification.class))).thenReturn(List.of(salary));
		when(salaryMapper.toResponse(salary)).thenReturn(response);

		List<SalaryResponse> result = salaryService.findAll(
				SalaryType.PJ,
				SalaryStatus.RECEIVED,
				2026,
				8);

		assertEquals(1, result.size());
		assertEquals(SalaryType.CLT, result.getFirst().type());
	}

	private Salary buildSalary(Long id) {
		Salary salary = new Salary();
		salary.setId(id);
		salary.setUserId(USER_ID);
		salary.setType(SalaryType.CLT);
		salary.setDescription("Empresa X");
		salary.setAmount(new BigDecimal("5000.00"));
		salary.setPaymentDate(LocalDate.of(2026, 8, 5));
		salary.setStatus(SalaryStatus.PENDING);
		salary.setCreatedAt(Instant.parse("2026-08-10T12:00:00Z"));
		salary.setUpdatedAt(Instant.parse("2026-08-10T12:00:00Z"));
		return salary;
	}

	private SalaryResponse buildResponse(Long id) {
		return new SalaryResponse(
				id,
				SalaryType.CLT,
				"Empresa X",
				new BigDecimal("5000.00"),
				LocalDate.of(2026, 8, 5),
				SalaryStatus.PENDING,
				Instant.parse("2026-08-10T12:00:00Z"),
				Instant.parse("2026-08-10T12:00:00Z"));
	}
}
