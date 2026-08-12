package com.financecontrol.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "import_batches")
public class ImportBatch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "file_name", nullable = false, length = 255)
	private String fileName;

	@Column(name = "imported_at", nullable = false, updatable = false)
	private Instant importedAt;

	@Column(name = "row_count", nullable = false)
	private int rowCount;

	@Column(name = "skipped_count", nullable = false)
	private int skippedCount;

	@Column(name = "reference_year", nullable = false)
	private int referenceYear;

	@Column(name = "reference_month", nullable = false)
	private int referenceMonth;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@OneToMany(mappedBy = "importBatch", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CreditCardTransaction> transactions = new ArrayList<>();

	@PrePersist
	void onCreate() {
		if (importedAt == null) {
			importedAt = Instant.now();
		}
	}

	public void addTransaction(CreditCardTransaction transaction) {
		transactions.add(transaction);
		transaction.setImportBatch(this);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Instant getImportedAt() {
		return importedAt;
	}

	public void setImportedAt(Instant importedAt) {
		this.importedAt = importedAt;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getSkippedCount() {
		return skippedCount;
	}

	public void setSkippedCount(int skippedCount) {
		this.skippedCount = skippedCount;
	}

	public int getReferenceYear() {
		return referenceYear;
	}

	public void setReferenceYear(int referenceYear) {
		this.referenceYear = referenceYear;
	}

	public int getReferenceMonth() {
		return referenceMonth;
	}

	public void setReferenceMonth(int referenceMonth) {
		this.referenceMonth = referenceMonth;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public List<CreditCardTransaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<CreditCardTransaction> transactions) {
		this.transactions = transactions;
	}
}
