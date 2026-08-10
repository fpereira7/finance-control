package com.financecontrol.controller;

import com.financecontrol.dto.MonthlyExpenseRequest;
import com.financecontrol.dto.MonthlyExpenseResponse;
import com.financecontrol.entity.ExpenseCategory;
import com.financecontrol.entity.PaymentStatus;
import com.financecontrol.service.MonthlyExpenseService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/monthly-expenses")
public class MonthlyExpenseController {

	private final MonthlyExpenseService monthlyExpenseService;

	public MonthlyExpenseController(MonthlyExpenseService monthlyExpenseService) {
		this.monthlyExpenseService = monthlyExpenseService;
	}

	@GetMapping
	public ResponseEntity<List<MonthlyExpenseResponse>> findAll(
			@RequestParam(required = false) ExpenseCategory category,
			@RequestParam(required = false) PaymentStatus status,
			@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month) {
		return ResponseEntity.ok(monthlyExpenseService.findAll(category, status, year, month));
	}

	@GetMapping("/{id}")
	public ResponseEntity<MonthlyExpenseResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(monthlyExpenseService.findById(id));
	}

	@PostMapping
	public ResponseEntity<MonthlyExpenseResponse> create(@Valid @RequestBody MonthlyExpenseRequest request) {
		MonthlyExpenseResponse created = monthlyExpenseService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{id}")
	public ResponseEntity<MonthlyExpenseResponse> update(
			@PathVariable Long id,
			@Valid @RequestBody MonthlyExpenseRequest request) {
		return ResponseEntity.ok(monthlyExpenseService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		monthlyExpenseService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
