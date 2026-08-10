package com.financecontrol.mapper;

import com.financecontrol.dto.SalaryRequest;
import com.financecontrol.dto.SalaryResponse;
import com.financecontrol.entity.Salary;
import org.springframework.stereotype.Component;

@Component
public class SalaryMapper {

	public Salary toEntity(SalaryRequest request) {
		Salary salary = new Salary();
		apply(request, salary);
		return salary;
	}

	public void updateEntity(Salary salary, SalaryRequest request) {
		apply(request, salary);
	}

	public SalaryResponse toResponse(Salary salary) {
		return new SalaryResponse(
				salary.getId(),
				salary.getType(),
				salary.getDescription(),
				salary.getAmount(),
				salary.getPaymentDate(),
				salary.getStatus(),
				salary.getCreatedAt(),
				salary.getUpdatedAt()
		);
	}

	private void apply(SalaryRequest request, Salary salary) {
		salary.setType(request.type());
		salary.setDescription(request.description());
		salary.setAmount(request.amount());
		salary.setPaymentDate(request.paymentDate());
		salary.setStatus(request.status());
	}
}
