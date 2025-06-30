package com.banking.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * debit and credit type enumeration.
 */
public enum DebitCredit {
    DEBIT("DR", "Debit, money outflow"),
    CREDIT("CR", "Credit, money inflow");
    
    private final String code;
    private final String description;
    
    DebitCredit(String code, String description) {
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
    public static DebitCredit fromCode(String code) {
        for (DebitCredit dc : DebitCredit.values()) {
            if (dc.code.equals(code)) {
                return dc;
            }
        }
        throw new IllegalArgumentException("Unknown debit/credit code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
} 