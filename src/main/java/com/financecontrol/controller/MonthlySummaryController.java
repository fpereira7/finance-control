package com.financecontrol.controller;

import com.financecontrol.dto.MonthlySummaryResponse;
import com.financecontrol.service.MonthlySummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monthly-summary")
public class MonthlySummaryController {

	private final MonthlySummaryService monthlySummaryService;

	public MonthlySummaryController(MonthlySummaryService monthlySummaryService) {
		this.monthlySummaryService = monthlySummaryService;
	}

	@GetMapping
	public ResponseEntity<MonthlySummaryResponse> getSummary(
			@RequestParam int year,
			@RequestParam int month) {
		return ResponseEntity.ok(monthlySummaryService.getSummary(year, month));
	}
}
