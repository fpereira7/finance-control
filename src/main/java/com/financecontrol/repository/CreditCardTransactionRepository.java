package com.financecontrol.repository;

import com.financecontrol.entity.CreditCardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditCardTransactionRepository extends JpaRepository<CreditCardTransaction, Long> {
}
