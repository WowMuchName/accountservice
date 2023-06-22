package com.cbbank.accountservice.repo;

import com.cbbank.accountservice.entity.FinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialTransactionRepo extends JpaRepository<FinancialTransaction, String> {
}
