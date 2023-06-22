package com.cbbank.accountservice.repo;

import com.cbbank.accountservice.entity.NonceToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NonceTokenRepo extends JpaRepository<NonceToken, String> {
}
