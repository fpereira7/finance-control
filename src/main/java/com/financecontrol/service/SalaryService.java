package com.financecontrol.service;

import com.financecontrol.dto.SalaryRequest;
import com.financecontrol.dto.SalaryResponse;
import com.financecontrol.entity.Salary;
import com.financecontrol.entity.SalaryStatus;
import com.financecontrol.entity.SalaryType;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.mapper.SalaryMapper;
import com.financecontrol.repository.SalaryRepository;
import com.financecontrol.security.AuthenticatedUserAccessor;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalaryService {

	private final SalaryRepository salaryRepository;
	private final SalaryMapper salaryMapper;
	private final AuthenticatedUserAccessor authenticatedUserAccessor;

	public SalaryService(
			SalaryRepository salaryRepository,
			SalaryMapper salaryMapper,
			AuthenticatedUserAccessor authenticatedUserAccessor) {
		this.salaryRepository = salaryRepository;
		this.salaryMapper = salaryMapper;
		this.authenticatedUserAccessor = authenticatedUserAccessor;
	}

	@Transactional(readOnly = true)
	public List<SalaryResponse> findAll(
			SalaryType type,
			SalaryStatus status,
			Integer year,
			Integer month) {
		validateYearMonth(year, month);
		Long userId = authenticatedUserAccessor.requireUserId();

		return salaryRepository
				.findAll(SalaryRepository.withFilters(userId, type, status, year, month))
				.stream()
				.map(salaryMapper::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public SalaryResponse findById(Long id) {
		return salaryMapper.toResponse(getOrThrow(id));
	}

	@Transactional
	public SalaryResponse create(SalaryRequest request) {
		Long userId = authenticatedUserAccessor.requireUserId();
		Salary salary = salaryMapper.toEntity(request);
		salary.setUserId(userId);
		Salary saved = salaryRepository.save(salary);
		return salaryMapper.toResponse(saved);
	}

	@Transactional
	public SalaryResponse update(Long id, SalaryRequest request) {
		Salary salary = getOrThrow(id);
		salaryMapper.updateEntity(salary, request);
		return salaryMapper.toResponse(salary);
	}

	@Transactional
	public void delete(Long id) {
		Salary salary = getOrThrow(id);
		salaryRepository.delete(salary);
	}

	private Salary getOrThrow(Long id) {
		Long userId = authenticatedUserAccessor.requireUserId();
		return salaryRepository.findByIdAndUserId(id, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Salary not found: " + id));
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
