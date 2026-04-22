package com.quickbite.payment_service.repository;

import com.quickbite.payment_service.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
