package com.banking.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Transaction status enumeration
 */
public enum TransactionStatus {
    PENDING("P", "Pending to be processed"),
    COMPLETED("C", "Completed"),
    FAILED("F", "Failed");
    
    private final String code;
    private final String description;
    
    TransactionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    @JsonValue
    public String getCode() {
        return code;
    }
    
    public String getDescription() { 
        return description; 
    }
    
    @JsonCreator
    public static TransactionStatus fromCode(String code) {
        for (TransactionStatus status : TransactionStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown transaction status code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
} 