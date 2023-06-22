package com.cbbank.accountservice.service;

import com.cbbank.accountservice.entity.Account;
import com.cbbank.accountservice.repo.AccountRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AccountService {
    AccountRepo accountRepo;

    public Optional<Account> getAccount(String id) {
        return accountRepo.findById(id);
    }
}
