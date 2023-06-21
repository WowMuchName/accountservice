package com.cbbank.accountservice.service;

import com.cbbank.accountservice.entity.DummyEntity;
import com.cbbank.accountservice.repo.DummyRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DummyService {
    DummyRepo repo;

    public DummyEntity create(DummyEntity entity) {
        return repo.save(entity);
    }

    public List<DummyEntity> list() {
        return repo.findAll();
    }
}
