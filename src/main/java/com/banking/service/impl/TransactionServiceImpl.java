package com.banking.service.impl;

import com.banking.dto.PagedResult;
import com.banking.enums.TransactionStatus;
import com.banking.exception.TransactionNotFoundException;
import com.banking.model.Transaction;
import com.banking.repository.TransactionRepository;
import com.banking.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Transaction service implementation with optimized caching strategy
 * Cache strategy for banking transactions:
 * - Short TTL for data consistency
 * - Precise cache eviction to avoid unnecessary cache clearing
 * - No caching for frequently changing data (pagination)
 */
@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CacheManager cacheManager;

    @Override
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        try {
            transaction.setStatus(TransactionStatus.PENDING);
            LocalDateTime currentTime = LocalDateTime.now();
            transaction.setCreatedAt(currentTime);
            transaction.setUpdatedAt(null);
            Transaction transactionSaved = transactionRepository.save(transaction);
            logger.info("Transaction created successfully, id {}, transaction number {}", transactionSaved.getId(), transactionSaved.getTradeNo());
            return transactionSaved;
        } catch (Exception e) {
            logger.error("Failed to create transaction, transaction number {}, error {}: ", transaction.getTradeNo(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Cacheable(value = "transaction-by-id", key = "#id", unless = "#result == null")
    public Transaction getTransactionById(Long id) {
        logger.info("Getting transaction by ID {}", id);
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found by ID " + id));
    }

    @Override
    @Cacheable(value = "transaction-by-tradeno", key = "#tradeNo", unless = "#result == null")
    public Transaction getTransactionByTradeNo(String tradeNo) {
        logger.info("Getting transaction by trade number {}", tradeNo);
        return transactionRepository.findByTradeNo(tradeNo)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found by trade number: " + tradeNo));
    }

    @Override
    @Transactional
    @CacheEvict(value = "transaction-by-id", key = "#id")
    public Transaction updateTransaction(Long id, Transaction transaction) {
        try {
            Transaction existingTransaction = getTransactionById(id);

            existingTransaction.setAccountNumber(transaction.getAccountNumber());
            existingTransaction.setAccountName(transaction.getAccountName());
            existingTransaction.setPayeeAccount(transaction.getPayeeAccount());
            existingTransaction.setPayeeName(transaction.getPayeeName());
            existingTransaction.setAmount(transaction.getAmount());
            existingTransaction.setCurrency(transaction.getCurrency());
            existingTransaction.setType(transaction.getType());
            existingTransaction.setDescription(transaction.getDescription());

            existingTransaction.setUpdatedAt(LocalDateTime.now());

            Transaction updatedTransaction = transactionRepository.save(existingTransaction);

            //clear cache
            String tradeNoToEvict = existingTransaction.getTradeNo();
            if (cacheManager != null && cacheManager.getCache("transaction-by-tradeno") != null) {
                cacheManager.getCache("transaction-by-tradeno").evict(tradeNoToEvict);
            }

            logger.info("Transaction updated successfully, ID {}, trade number {}", id, updatedTransaction.getTradeNo());
            return updatedTransaction;

        } catch (Exception e) {
            logger.error("Failed to update transaction, ID {}, trade number {},  Error: {}", id, transaction.getTradeNo(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "transaction-by-tradeno", key = "#tradeNo")
    public Transaction updateTransactionByTradeNo(String tradeNo, Transaction transaction) {
        logger.info("Updating transaction by trade number {}", tradeNo);
        try {
            Transaction existingTransaction = getTransactionByTradeNo(tradeNo);

            existingTransaction.setAccountNumber(transaction.getAccountNumber());
            existingTransaction.setAccountName(transaction.getAccountName());
            existingTransaction.setPayeeAccount(transaction.getPayeeAccount());
            existingTransaction.setPayeeName(transaction.getPayeeName());
            existingTransaction.setAmount(transaction.getAmount());
            existingTransaction.setCurrency(transaction.getCurrency());
            existingTransaction.setType(transaction.getType());
            existingTransaction.setDescription(transaction.getDescription());
            existingTransaction.setDebitCredit(transaction.getDebitCredit());

            existingTransaction.setUpdatedAt(LocalDateTime.now());

            Transaction updatedTransaction = transactionRepository.save(existingTransaction);

            //should also clear cache for id
            Long idToEvict = existingTransaction.getId();
            if (cacheManager != null && cacheManager.getCache("transaction-by-id") != null) {
                cacheManager.getCache("transaction-by-id").evict(idToEvict);
            }

            logger.info("Transaction updated successfully, id {}, trade number {}", updatedTransaction.getId(), tradeNo);
            return updatedTransaction;

        } catch (Exception e) {
            logger.error("Failed to update transaction - id {}, trade number {}, Error: {}", transaction.getId(), tradeNo, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "transaction-by-id", key = "#id")
    public void deleteTransaction(Long id) {
        try {
            Transaction transaction = getTransactionById(id);
            transactionRepository.delete(transaction);

            //should also clear cache for TradeNo
            String tradeNo = transaction.getTradeNo();
            if (cacheManager != null && cacheManager.getCache("transaction-by-tradeno") != null) {
                cacheManager.getCache("transaction-by-tradeno").evict(tradeNo);
            }

            logger.info("Transaction deleted successfully by ID {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete transaction by ID {}, error: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "transaction-by-tradeno", key = "#tradeNo")
    public void deleteTransactionByTradeNo(String tradeNo) {
        try {
            Transaction transaction = getTransactionByTradeNo(tradeNo);
            Long id = transaction.getId();

            transactionRepository.delete(transaction);

            //should also clear cache for id
            if (cacheManager != null && cacheManager.getCache("transaction-by-id") != null) {
                cacheManager.getCache("transaction-by-id").evict(id);
            }

            logger.info("Transaction deleted successfully by trade number {}", tradeNo);
        } catch (Exception e) {
            logger.error("Failed to delete transaction by trade number {}, error: {}", tradeNo, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public PagedResult<Transaction> getTransactions(int page, int size) {
        logger.info("Getting transaction list - page {}, size {}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Transaction> transactionPage = transactionRepository.findAll(pageable);
            PagedResult<Transaction> result = new PagedResult<>(
                    transactionPage.getContent(),
                    transactionPage.getNumber(),
                    transactionPage.getSize(),
                    transactionPage.getTotalElements(),
                    transactionPage.getTotalPages()
            );
            logger.info("Retrieved {} transactions by page {}, size {}", result.getContent().size(), page, size);
            return result;

        } catch (Exception e) {
            logger.error("Failed to get transactions by page {}, size {}, error: {}", page, size, e.getMessage(), e);
            throw e;
        }
    }
} 