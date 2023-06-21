package com.cbbank.accountservice.rest;

import com.cbbank.accountservice.entity.DummyEntity;
import com.cbbank.accountservice.service.DummyService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("dummy")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DummyRestController {
    DummyService service;

    @PutMapping
    public DummyEntity create(@RequestBody DummyEntity entity) {
        return service.create(entity);
    }

    @GetMapping
    public List<DummyEntity> list() {
        return service.list();
    }
}
