package com.quickbite.payment_service.controller;

import com.quickbite.payment_service.dto.WalletRequest;
import com.quickbite.payment_service.dto.WalletResponse;
import com.quickbite.payment_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{userId}")
    public WalletResponse getWallet(@PathVariable String userId) {
        return walletService.getWalletByUserId(userId);
    }

    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse createWallet(@PathVariable String userId) {
        return walletService.createWallet(userId);
    }

    @PostMapping("/{userId}/deposit")
    public WalletResponse deposit(@PathVariable String userId, @Valid @RequestBody WalletRequest request) {
        return walletService.deposit(userId, request);
    }

    @PostMapping("/{userId}/withdraw")
    public WalletResponse withdraw(@PathVariable String userId, @Valid @RequestBody WalletRequest request) {
        return walletService.withdraw(userId, request);
    }
}
