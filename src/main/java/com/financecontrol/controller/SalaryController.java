package com.financecontrol.controller;

import com.financecontrol.dto.SalaryRequest;
import com.financecontrol.dto.SalaryResponse;
import com.financecontrol.entity.SalaryStatus;
import com.financecontrol.entity.SalaryType;
import com.financecontrol.service.SalaryService;
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
@RequestMapping("/api/salaries")
public class SalaryController {

	private final SalaryService salaryService;

	public SalaryController(SalaryService salaryService) {
		this.salaryService = salaryService;
	}

	@GetMapping
	public ResponseEntity<List<SalaryResponse>> findAll(
			@RequestParam(required = false) SalaryType type,
			@RequestParam(required = false) SalaryStatus status,
			@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month) {
		return ResponseEntity.ok(salaryService.findAll(type, status, year, month));
	}

	@GetMapping("/{id}")
	public ResponseEntity<SalaryResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(salaryService.findById(id));
	}

	@PostMapping
	public ResponseEntity<SalaryResponse> create(@Valid @RequestBody SalaryRequest request) {
		SalaryResponse created = salaryService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{id}")
	public ResponseEntity<SalaryResponse> update(
			@PathVariable Long id,
			@Valid @RequestBody SalaryRequest request) {
		return ResponseEntity.ok(salaryService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		salaryService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
