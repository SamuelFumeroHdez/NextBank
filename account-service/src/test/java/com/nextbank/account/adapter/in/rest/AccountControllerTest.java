package com.nextbank.account.adapter.in.rest;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import tools.jackson.databind.ObjectMapper;
import com.nextbank.account.application.port.in.ModifyBalanceUseCase;
import com.nextbank.account.application.port.in.ModifyBalanceUseCase.BalanceCommand;
import com.nextbank.account.application.port.in.OpenAccountUseCase;
import com.nextbank.account.application.port.in.OpenAccountUseCase.OpenAccountCommand;
import com.nextbank.account.domain.Account;
import com.nextbank.account.domain.AccountId;
import com.nextbank.account.domain.AccountNotFoundException;
import com.nextbank.account.domain.CustomerId;
import com.nextbank.account.domain.Iban;
import com.nextbank.account.domain.InsufficientFundsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(ApiExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("API de cuentas")
class AccountControllerTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final String VALID_IBAN = "ES9121000418450200051332";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean OpenAccountUseCase openAccountUseCase;
    @MockitoBean ModifyBalanceUseCase modifyBalanceUseCase;

    private Account anAccount(String balance) {
        return Account.reconstitute(
                AccountId.of("acc_test_001"),
                new CustomerId("cus_001"),
                new Iban(VALID_IBAN),
                EUR,
                new BigDecimal(balance),
                Instant.parse("2026-08-07T10:00:00Z"),
                0L);
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    // ---------- Apertura ----------

    @Test
    @DisplayName("devuelve 201 con los datos de la cuenta creada")
    void returns201OnAccountCreation() throws Exception {
        given(openAccountUseCase.open(any())).willReturn(anAccount("0.00"));

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "customerId", "cus_001",
                                "iban", VALID_IBAN,
                                "currency", "EUR"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value("acc_test_001"))
                .andExpect(jsonPath("$.customerId").value("cus_001"))
                .andExpect(jsonPath("$.iban").value(VALID_IBAN))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.balance").value("0.00"));
    }

    @Test
    @DisplayName("devuelve 400 si el IBAN no es valido")
    void returns400OnInvalidIban() throws Exception {
        willThrow(new IllegalArgumentException("Invalid IBAN checksum: ES0000000000000000000000"))
                .given(openAccountUseCase).open(any(OpenAccountCommand.class));

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "customerId", "cus_001",
                                "iban", "ES0000000000000000000000",
                                "currency", "EUR"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    // ---------- Ingresos y cargos ----------

    @Test
    @DisplayName("devuelve 200 y el saldo actualizado tras un ingreso")
    void returns200OnCredit() throws Exception {
        given(modifyBalanceUseCase.credit(any())).willReturn(anAccount("150.00"));

        mockMvc.perform(post("/accounts/acc_test_001/credit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "amount", "150.00",
                                "idempotencyKey", "key-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value("150.00"));
    }

    @Test
    @DisplayName("devuelve 200 y el saldo actualizado tras un cargo")
    void returns200OnDebit() throws Exception {
        given(modifyBalanceUseCase.debit(any())).willReturn(anAccount("70.00"));

        mockMvc.perform(post("/accounts/acc_test_001/debit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "amount", "30.00",
                                "idempotencyKey", "key-2"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value("70.00"));
    }

    @Test
    @DisplayName("devuelve 422 con el motivo si no hay saldo suficiente")
    void returns422OnInsufficientFunds() throws Exception {
        willThrow(new InsufficientFundsException(
                AccountId.of("acc_test_001"),
                new BigDecimal("500.00"),
                new BigDecimal("70.00")))
                .given(modifyBalanceUseCase).debit(any(BalanceCommand.class));

        mockMvc.perform(post("/accounts/acc_test_001/debit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "amount", "500.00",
                                "idempotencyKey", "key-3"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.reason").value("INSUFFICIENT_FUNDS"));
    }

    @Test
    @DisplayName("devuelve 404 si la cuenta no existe")
    void returns404OnUnknownAccount() throws Exception {
        willThrow(new AccountNotFoundException("acc_unknown"))
                .given(modifyBalanceUseCase).credit(any(BalanceCommand.class));

        mockMvc.perform(post("/accounts/acc_unknown/credit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "amount", "10.00",
                                "idempotencyKey", "key-4"))))
                .andExpect(status().isNotFound());
    }
}