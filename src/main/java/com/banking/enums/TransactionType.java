package com.banking.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Transaction type enumeration </br>
 * Note: there are more transaction types in real banking system. Below are some common ones.
 */
public enum TransactionType {
    DEPOSIT("DP", "Deposit"),
    WITHDRAWAL("WD", "Withdrawal"),
    TRANSFER_OUT("TO", "Transfer Out"),
    TRANSFER_IN("TI", "Transfer In"),
    PAYMENT("PMT", "Payment"),
    REFUND("RF", "Refund"),
    FEE("FEE", "Fee"),
    INTEREST("INT", "Interest"),
    ADJUSTMENT("ADJ", "Adjustment"),
    CARD_PURCHASE("CP", "Card Purchase"),
    CARD_REFUND("CR", "Card Refund");
    
    private final String code;
    private final String description;
    
    TransactionType(String code, String description) {
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
    public static TransactionType fromCode(String code) {
        for (TransactionType type : TransactionType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown transaction type code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
} 