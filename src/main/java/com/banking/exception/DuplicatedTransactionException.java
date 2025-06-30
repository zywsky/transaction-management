package com.banking.exception;

/**
 * Exception thrown when a transaction is duplicated.
 */
public class DuplicatedTransactionException extends RuntimeException {
    public DuplicatedTransactionException(String message) {
        super(message);
    }
    
    public DuplicatedTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
} 