package com.nextbank.account.application;

import com.nextbank.account.IntegrationTestBase;
import com.nextbank.account.application.port.in.ModifyBalanceUseCase;
import com.nextbank.account.application.port.in.OpenAccountUseCase;
import com.nextbank.account.application.port.out.AccountRepositoryPort;
import com.nextbank.account.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("Concurrencia sobre el saldo")
public class ConcurrentBalanceTest extends IntegrationTestBase {

    @Autowired
    OpenAccountUseCase openAccountUseCase;
    @Autowired
    ModifyBalanceUseCase modifyBalanceUseCase;
    @Autowired
    AccountRepositoryPort accountRepository;

    @Test
    @DisplayName("dos cargos simultaneos no pueden dejar el saldo en negativo")
    void concurrentDebitsCannotOverdraw() throws Exception {
        Account account = openAccountUseCase.open(new OpenAccountUseCase.OpenAccountCommand(
                "cus_conc", "ES1000492352082414205416", Currency.getInstance("EUR")));
        String accountId = account.getAccountId().toString();

        modifyBalanceUseCase.credit(new ModifyBalanceUseCase.BalanceCommand(accountId, new BigDecimal("100.00"), "init"));

        int threads = 2;
        BigDecimal amount = new BigDecimal("80.00");

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finishGate = new CountDownLatch(threads);
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            final int index = i;
            pool.submit(() -> {
                try {
                    startGate.await();
                    modifyBalanceUseCase.debit(
                            new ModifyBalanceUseCase.BalanceCommand(accountId, amount, "concurrent-" + index));
                    succeeded.incrementAndGet();
                } catch (Exception e) {
                    failed.incrementAndGet();
                } finally {
                    finishGate.countDown();
                }
            });
        }

        startGate.countDown();                       // los lanza a la vez
        finishGate.await(10, TimeUnit.SECONDS);
        pool.shutdown();

        Account finalState = accountRepository.findById(account.getAccountId()).orElseThrow();

        assertThat(succeeded.get()).isEqualTo(1);
        assertThat(failed.get()).isEqualTo(1);
        assertThat(finalState.getBalance()).isEqualByComparingTo("20.00");
        assertThat(finalState.getBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }
}
