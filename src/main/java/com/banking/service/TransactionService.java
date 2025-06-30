package com.banking.service;

import com.banking.dto.PagedResult;
import com.banking.model.Transaction;

/**
 * the transaction service interface
 */
public interface TransactionService {
    Transaction createTransaction(Transaction transaction);

    Transaction getTransactionById(Long id);

    Transaction getTransactionByTradeNo(String tradeNo);

    Transaction updateTransaction(Long id, Transaction transaction);

    Transaction updateTransactionByTradeNo(String tradeNo, Transaction transaction);

    void deleteTransaction(Long id);

    void deleteTransactionByTradeNo(String tradeNo);

    PagedResult<Transaction> getTransactions(int page, int size);
}