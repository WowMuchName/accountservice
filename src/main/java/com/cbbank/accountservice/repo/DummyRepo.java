package com.cbbank.accountservice.repo;

import com.cbbank.accountservice.entity.DummyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DummyRepo extends JpaRepository<DummyEntity, String> {
}
