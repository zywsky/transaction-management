package com.banking.repository;

import com.banking.enums.TransactionType;
import com.banking.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTradeNo(String tradeNo);

    List<Transaction> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);

    Page<Transaction> findByAccountNumber(String accountNumber, Pageable pageable);

    Page<Transaction> findByAccountNumberOrderByCreatedAtDesc(String accountNumber, Pageable pageable);

    List<Transaction> findByTypeOrderByCreatedAtDesc(TransactionType type);

    List<Transaction> findAllByOrderByCreatedAtDesc();

    @Query("SELECT t FROM Transaction t WHERE t.accountNumber = :accountNumber AND t.type = :type ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountNumberAndType(@Param("accountNumber") String accountNumber, @Param("type") TransactionType type);

    long countByAccountNumber(String accountNumber);

    long countByType(TransactionType type);
} 