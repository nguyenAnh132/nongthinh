package com.nongthinh.rice_disease_diagnosis_service.configuration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class InternalApiKeyVerifierTest {

    private final InternalApiKeyVerifier verifier = new InternalApiKeyVerifier("diagnosis");

    @Test
    void acceptsTheDiagnosisServiceIncomingKey() {
        assertThatCode(() -> verifier.verify("diagnosis")).doesNotThrowAnyException();
    }

    @Test
    void rejectsTheCatalogServiceKeyUsedForOutboundCalls() {
        assertThatThrownBy(() -> verifier.verify("agri-catalog"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException response = (ResponseStatusException) exception;
                    org.assertj.core.api.Assertions.assertThat(response.getStatusCode())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }
}
