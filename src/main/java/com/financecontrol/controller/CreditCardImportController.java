package com.financecontrol.controller;

import com.financecontrol.dto.CreditCardImportDetailResponse;
import com.financecontrol.dto.CreditCardImportResponse;
import com.financecontrol.dto.CreditCardImportSummaryResponse;
import com.financecontrol.service.CreditCardImportService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/credit-card-imports")
public class CreditCardImportController {

	private final CreditCardImportService creditCardImportService;

	public CreditCardImportController(CreditCardImportService creditCardImportService) {
		this.creditCardImportService = creditCardImportService;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<CreditCardImportResponse> importCsv(@RequestPart("file") MultipartFile file) {
		CreditCardImportResponse response = creditCardImportService.importCsv(file);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<CreditCardImportSummaryResponse>> findByMonth(
			@RequestParam int year,
			@RequestParam int month) {
		return ResponseEntity.ok(creditCardImportService.findByMonth(year, month));
	}

	@GetMapping("/{id}")
	public ResponseEntity<CreditCardImportDetailResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(creditCardImportService.findById(id));
	}
}
