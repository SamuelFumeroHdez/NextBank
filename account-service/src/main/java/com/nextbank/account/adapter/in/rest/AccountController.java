package com.nextbank.account.adapter.in.rest;

import com.nextbank.account.application.port.in.ModifyBalanceUseCase;
import com.nextbank.account.application.port.in.OpenAccountUseCase;
import com.nextbank.account.domain.Account;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Currency;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final OpenAccountUseCase openAccountUseCase;
    private final ModifyBalanceUseCase modifyBalanceUseCase;


    public AccountController(OpenAccountUseCase openAccountUseCase, ModifyBalanceUseCase modifyBalanceUseCase) {
        this.openAccountUseCase = openAccountUseCase;
        this.modifyBalanceUseCase = modifyBalanceUseCase;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> open(@RequestBody OpenAccountRequest request){
        Account account = openAccountUseCase.open(new OpenAccountUseCase.OpenAccountCommand(
                request.customerId(),
                request.iban(),
                Currency.getInstance(request.currency())
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(account));
    }

    @PostMapping("/{accountId}/debit")
    public ResponseEntity<AccountResponse> debit(@PathVariable("accountId") String accountId,
                                                 @RequestBody BalanceRequest balanceRequest){
        Account account = modifyBalanceUseCase.debit(new ModifyBalanceUseCase.BalanceCommand(
                accountId, balanceRequest.amount(), balanceRequest.idempotenceKey()
        ));

        return ResponseEntity.ok(AccountResponse.from(account));
    }

    @PostMapping("/{accountId}/credit")
    public ResponseEntity<AccountResponse> credit(@PathVariable("accountId") String accountId,
                                                 @RequestBody BalanceRequest balanceRequest){
        Account account = modifyBalanceUseCase.credit(new ModifyBalanceUseCase.BalanceCommand(
                accountId, balanceRequest.amount(), balanceRequest.idempotenceKey()
        ));

        return ResponseEntity.ok(AccountResponse.from(account));
    }

    public record OpenAccountRequest(String customerId, String iban, String currency){}

    public record AccountResponse(String accountId, String customerId, String iban, String currency, String balance){
        static AccountResponse from(Account account){
            return new AccountResponse(
                    account.getAccountId().toString(),
                    account.getCustomerId().toString(),
                    account.getIban().toString(),
                    account.getCurrency().getCurrencyCode(),
                    account.getBalance().toString()
            );
        }
    }

    public record BalanceRequest(BigDecimal amount, String idempotenceKey){}


}
