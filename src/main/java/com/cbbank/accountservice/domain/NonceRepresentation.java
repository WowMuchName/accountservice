package com.cbbank.accountservice.domain;

import com.cbbank.accountservice.entity.NonceToken;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@RequiredArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NonceRepresentation {
    public static final String NONCE_MIME = "application/vnd.com.cbbank.accountservice.nonce-v1+json";
    String id;
    Long time;

    public static class NonceRepresentationBuilder {
        public NonceRepresentationBuilder from(NonceToken nonceToken) {
            return id(nonceToken.getId()).time(nonceToken.getTime());
        }
    }
}
