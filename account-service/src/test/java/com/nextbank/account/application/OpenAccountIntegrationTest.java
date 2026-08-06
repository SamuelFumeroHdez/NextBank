package com.nextbank.account.application;

import com.nextbank.account.IntegrationTestBase;
import com.nextbank.account.adapter.in.rest.CorrelationContext;
import com.nextbank.account.adapter.out.outbox.OutboxJpaRepository;
import com.nextbank.account.adapter.out.persistance.AccountJpaRepository;
import com.nextbank.account.application.port.in.OpenAccountUseCase;
import com.nextbank.account.application.port.out.AccountRepositoryPort;
import com.nextbank.account.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Apertura de cuenta - integracion")
public class OpenAccountIntegrationTest extends IntegrationTestBase {

    @Autowired
    OpenAccountUseCase openAccountUseCase;
    @Autowired
    AccountRepositoryPort accountRepository;
    @Autowired
    AccountJpaRepository accountJpaRepository;
    @Autowired
    OutboxJpaRepository outboxRepository;
    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        outboxRepository.deleteAll();
        accountJpaRepository.deleteAll();
    }

    @AfterEach
    void clearCorrelation() {
        CorrelationContext.clear();
    }

    @Test
    @DisplayName("persiste la cuenta y el evento en la misma operacion")
    void persistsAccountAndEventTogether() {
        openAccountUseCase.open(new OpenAccountUseCase.OpenAccountCommand(
                "cus_001", "ES9121000418450200051332", Currency.getInstance("EUR")));

        assertThat(accountJpaRepository.count()).isEqualTo(1);
        assertThat(outboxRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("la cuenta se recupera con los mismos datos")
    void accountIsRetrievableWithSameData() {
        Account opened = openAccountUseCase.open(new OpenAccountUseCase.OpenAccountCommand(
                "cus_002", "DE89370400440532013000", Currency.getInstance("EUR")));

        Account found = accountRepository.findById(opened.getAccountId()).orElseThrow();

        assertThat(found.getAccountId()).isEqualTo(opened.getAccountId());
        assertThat(found.getIban()).isEqualTo(opened.getIban());
        assertThat(found.getCustomerId()).isEqualTo(opened.getCustomerId());
        assertThat(found.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(found.getOpenedAt()).isNotNull();
    }

    @Test
    @DisplayName("el evento queda pendiente de publicar")
    void eventIsLeftUnpublished() {
        openAccountUseCase.open(new OpenAccountUseCase.OpenAccountCommand(
                "cus_003", "NL91ABNA0417164300", Currency.getInstance("EUR")));

        var event = outboxRepository.findAll().getFirst();

        assertThat(event.isPublished()).isFalse();
        assertThat(event.getEventType()).isEqualTo("account.account-opened.v1");
        assertThat(event.getAggregateType()).isEqualTo("Account");
        assertThat(event.getEventId()).isNotBlank();
        assertThat(event.getOccurredAt()).isNotNull();
    }

    @Test
    @DisplayName("el payload del evento sigue el catalogo de eventos")
    void eventPayloadMatchesCatalogue() throws Exception {
        Account opened = openAccountUseCase.open(new OpenAccountUseCase.OpenAccountCommand(
                "cus_004", "GB29NWBK60161331926819", Currency.getInstance("GBP")));

        var event = outboxRepository.findAll().getFirst();
        JsonNode payload = objectMapper.readTree(event.getPayload());

        assertThat(payload.get("accountId").asText()).isEqualTo(opened.getAccountId().toString());
        assertThat(payload.get("customerId").asText()).isEqualTo("cus_004");
        assertThat(payload.get("iban").asText()).isEqualTo("GB29NWBK60161331926819");
        assertThat(payload.get("currency").asText()).isEqualTo("GBP");
        assertThat(payload.has("openedAt")).isTrue();
    }

    @Test
    @DisplayName("el evento arrastra el identificador de correlacion")
    void eventCarriesCorrelationId() {
        CorrelationContext.set("corr-integration-test");

        openAccountUseCase.open(new OpenAccountUseCase.OpenAccountCommand(
                "cus_005", "PT50000201231234567890154", Currency.getInstance("EUR")));

        var event = outboxRepository.findAll().getFirst();

        assertThat(event.getCorrelationId()).isEqualTo("corr-integration-test");
    }

    @Test
    @DisplayName("un IBAN invalido no deja rastro en ninguna tabla")
    void invalidIbanLeavesNoTrace() {
        assertThatThrownBy(() -> openAccountUseCase.open(new OpenAccountUseCase.OpenAccountCommand(
                "cus_006", "ES9121000418450200051333", Currency.getInstance("EUR"))))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(accountJpaRepository.count()).isZero();
        assertThat(outboxRepository.count()).isZero();
    }

    @Test
    @DisplayName("no se pueden abrir dos cuentas con el mismo IBAN")
    void rejectsDuplicateIban() {
        OpenAccountUseCase.OpenAccountCommand command = new OpenAccountUseCase.OpenAccountCommand(
                "cus_007", "IT60X0542811101000000123456", Currency.getInstance("EUR"));

        openAccountUseCase.open(command);

        assertThatThrownBy(() -> openAccountUseCase.open(command))
                .isInstanceOf(Exception.class);

        assertThat(accountJpaRepository.count()).isEqualTo(1);
    }


}
