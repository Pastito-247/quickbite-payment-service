package com.quickbite.payment_service.service;

import com.quickbite.payment_service.dto.WalletRequest;
import com.quickbite.payment_service.dto.WalletResponse;
import com.quickbite.payment_service.entity.Wallet;
import com.quickbite.payment_service.exception.InsufficientFundsException;
import com.quickbite.payment_service.exception.ResourceNotFoundException;
import com.quickbite.payment_service.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletResponse getWalletByUserId(String userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Billetera no encontrada para el usuario: " + userId));
        return mapToResponse(wallet);
    }

    @Transactional
    public WalletResponse createWallet(String userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("El usuario ya tiene una billetera");
        }

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .currency("CLP")
                .build();

        Wallet savedWallet = walletRepository.save(wallet);
        return mapToResponse(savedWallet);
    }

    @Transactional
    public WalletResponse deposit(String userId, WalletRequest request) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Billetera no encontrada para el usuario: " + userId));

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        Wallet savedWallet = walletRepository.save(wallet);

        return mapToResponse(savedWallet);
    }

    @Transactional
    public WalletResponse withdraw(String userId, WalletRequest request) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Billetera no encontrada para el usuario: " + userId));

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Saldo insuficiente. Saldo actual: " + wallet.getBalance());
        }

        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        Wallet savedWallet = walletRepository.save(wallet);

        return mapToResponse(savedWallet);
    }

    @Transactional
    public WalletResponse deductBalance(String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Billetera no encontrada para el usuario: " + userId));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Saldo insuficiente para realizar el pago");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        Wallet savedWallet = walletRepository.save(wallet);

        return mapToResponse(savedWallet);
    }

    @Transactional
    public WalletResponse addBalance(String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Billetera no encontrada para el usuario: " + userId));

        wallet.setBalance(wallet.getBalance().add(amount));
        Wallet savedWallet = walletRepository.save(wallet);

        return mapToResponse(savedWallet);
    }

    private WalletResponse mapToResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}
