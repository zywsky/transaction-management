package com.banking.controller;

import com.banking.dto.PagedResult;
import com.banking.enums.DebitCredit;
import com.banking.enums.TransactionStatus;
import com.banking.enums.TransactionType;
import com.banking.exception.DuplicatedTransactionException;
import com.banking.exception.TransactionNotFoundException;
import com.banking.model.Transaction;
import com.banking.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TransactionController Unit test
 */
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Transaction validTransaction;
    private Transaction mockSavedDBTransaction;

    @BeforeEach
    void setUp() {
        //valid transaction
        validTransaction = new Transaction();
        validTransaction.setTradeNo("123456789012345654");
        validTransaction.setAccountNumber("1234567890123456");
        validTransaction.setAccountName("david");
        validTransaction.setPayeeAccount("9876543210987654");
        validTransaction.setPayeeName("Tom");
        validTransaction.setAmount(new BigDecimal("500.00"));
        validTransaction.setCurrency("CNY");
        validTransaction.setType(TransactionType.TRANSFER_OUT);
        validTransaction.setDebitCredit(DebitCredit.DEBIT);
        validTransaction.setDescription("Test");

        //same value duplicated transaction
        mockSavedDBTransaction = new Transaction();
        mockSavedDBTransaction.setId(1L);
        mockSavedDBTransaction.setTradeNo("123456789012345654");
        mockSavedDBTransaction.setAccountNumber("1234567890123456");
        mockSavedDBTransaction.setAccountName("david");
        mockSavedDBTransaction.setPayeeAccount("9876543210987654");
        mockSavedDBTransaction.setPayeeName("Tom");
        mockSavedDBTransaction.setAmount(new BigDecimal("500.00"));
        mockSavedDBTransaction.setCurrency("CNY");
        mockSavedDBTransaction.setType(TransactionType.TRANSFER_OUT);
        mockSavedDBTransaction.setDebitCredit(DebitCredit.DEBIT);
        mockSavedDBTransaction.setDescription("Test");
        mockSavedDBTransaction.setStatus(TransactionStatus.PENDING);
        mockSavedDBTransaction.setCreatedAt(LocalDateTime.now());
        mockSavedDBTransaction.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Create Transaction Tests")
    class CreateTransactionTests {

        @Test
        @DisplayName("Should create transaction successfully with valid data")
        void testCreateTransaction_Success() throws Exception {
            when(transactionService.createTransaction(any(Transaction.class))).thenReturn(mockSavedDBTransaction);

            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isCreated())                    
                    .andExpect(jsonPath("$.tradeNo").value("123456789012345654"))
                    .andExpect(jsonPath("$.accountNumber").value("1234567890123456"))
                    .andExpect(jsonPath("$.accountName").value("david"))
                    .andExpect(jsonPath("$.amount").value(500.00))
                    .andExpect(jsonPath("$.currency").value("CNY"))
                    .andExpect(jsonPath("$.type").value("TO"))
                    .andExpect(jsonPath("$.debitCredit").value("DR"))
                    .andExpect(jsonPath("$.status").value("P"));

            verify(transactionService, times(1)).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return duplicated transaction error when creating duplicate transaction - we consider duplicated if same trade number")
        void testCreateTransaction_DuplicateTradeNo() throws Exception {
            when(transactionService.createTransaction(any(Transaction.class)))
                    .thenThrow(new DuplicatedTransactionException("Transaction with trade number 123456789012345654 already exists"));

            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isConflict())
                    .andExpect(content().string(containsString("Transaction with trade number 123456789012345654 already exists")));

            verify(transactionService, times(1)).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when trade number is missing")
        void testCreateTransaction_MissingTradeNo() throws Exception {
            validTransaction.setTradeNo(null);
            mockMvc.perform(post("/transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Trade number is required")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when account number is missing")
        void testCreateTransaction_MissingAccountNumber() throws Exception {
            validTransaction.setAccountNumber(null);
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Account number is required")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when account name is missing")
        void testCreateTransaction_MissingAccountName() throws Exception {
            validTransaction.setAccountName(null);
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Account name is required")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when payee account is missing")
        void testCreateTransaction_MissingPayeeAccount() throws Exception {
            validTransaction.setPayeeAccount(null);
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Payee account is required")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when payee name is missing")
        void testCreateTransaction_MissingPayeeName() throws Exception {
            validTransaction.setPayeeName(null);
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Payee name is required")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when amount is missing")
        void testCreateTransaction_MissingAmount() throws Exception {
            validTransaction.setAmount(null);
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Amount is required")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when currency is missing")
        void testCreateTransaction_MissingCurrency() throws Exception {
            validTransaction.setCurrency(null);
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Currency is required")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when debit/credit indicator is missing")
        void testCreateTransaction_MissingDebitCredit() throws Exception {
            validTransaction.setDebitCredit(null);
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Debit/Credit indicator is required")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when trade number format is invalid")
        void testCreateTransaction_InvalidTradeNoFormat() throws Exception {
            validTransaction.setTradeNo("12345");

            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Invalid trade number format")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when account number is invalid")
        void testCreateTransaction_InvalidAccountNumber() throws Exception {
            validTransaction.setAccountNumber("123");

            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Account number must be between 10 and 20 characters")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when account name contains numbers")
        void testCreateTransaction_InvalidAccountName() throws Exception {
            validTransaction.setAccountName("John123"); // Contains numbers

            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Account name can only contain letters and spaces")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when amount is negative")
        void testCreateTransaction_NegativeAmount() throws Exception {
            validTransaction.setAmount(new BigDecimal("-100.50"));

            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Amount must be equal or greater than 0.01\"")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when currency format is invalid")
        void testCreateTransaction_InvalidCurrency() throws Exception {
            validTransaction.setCurrency("cn");

            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when transaction type is missing")
        void testCreateTransaction_MissingTransactionType() throws Exception {
            validTransaction.setType(null);

            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Transaction type is required")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 with multiple validation errors")
        void testCreateTransaction_MultipleValidationErrors() throws Exception {
            // Set multiple invalid values
            validTransaction.setTradeNo(null);
            validTransaction.setAccountName(null);
            validTransaction.setAmount(new BigDecimal("-100"));
            validTransaction.setCurrency("cn");

            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when trade number contains letters")
        void testCreateTransaction_TradeNoWithLetters() throws Exception {
            validTransaction.setTradeNo("12345678901234567A"); // Contains letter
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Invalid trade number format")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when account number contains letters")
        void testCreateTransaction_AccountNumberWithLetters() throws Exception {
            validTransaction.setAccountNumber("123456789A"); // Contains letter
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Account number can only contain digits")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when account number is too long")
        void testCreateTransaction_AccountNumberTooLong() throws Exception {
            validTransaction.setAccountNumber("123456789012345678901"); // 21 characters
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Account number must be between 10 and 20 characters")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when account name is too short")
        void testCreateTransaction_AccountNameTooShort() throws Exception {
            validTransaction.setAccountName("A"); // Only 1 character
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Account name must be between 2 and 100 characters")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when account name is too long")
        void testCreateTransaction_AccountNameTooLong() throws Exception {
            String tooLongName = "A".repeat(101); // 101 characters
            validTransaction.setAccountName(tooLongName);
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Account name must be between 2 and 100 characters")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when payee account is too short")
        void testCreateTransaction_PayeeAccountTooShort() throws Exception {
            validTransaction.setPayeeAccount("123456789"); // Only 9 characters
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Payee account must be between 10 and 20 characters")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when payee account contains letters")
        void testCreateTransaction_PayeeAccountWithLetters() throws Exception {
            validTransaction.setPayeeAccount("123456789A"); // Contains letter
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Payee account can only contain digits")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when payee name is too long")
        void testCreateTransaction_PayeeNameTooLong() throws Exception {
            String tooLongName = "A".repeat(101); // 101 characters
            validTransaction.setPayeeName(tooLongName);
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Payee name must be between 2 and 100 characters")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when amount has too many decimal places")
        void testCreateTransaction_AmountTooManyDecimals() throws Exception {
            validTransaction.setAmount(new BigDecimal("100.123")); // 3 decimal places
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Amount can have maximum 17 integer digits and 2 decimal places")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when amount has too many integer digits")
        void testCreateTransaction_AmountTooManyIntegerDigits() throws Exception {
            // 20 integer digits (exceeds @Digits(integer = 19) limit)
            validTransaction.setAmount(new BigDecimal("12345678901234567890.12"));
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Amount can have maximum 17 integer digits and 2 decimal places")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should accept minimum valid account number length")
        void testCreateTransaction_MinAccountNumberLength() throws Exception {
            validTransaction.setAccountNumber("1234567890"); // Exactly 10 characters
            when(transactionService.createTransaction(any(Transaction.class))).thenReturn(mockSavedDBTransaction);
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isCreated());

            verify(transactionService, times(1)).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should accept maximum valid account number length")
        void testCreateTransaction_MaxAccountNumberLength() throws Exception {
            validTransaction.setAccountNumber("12345678901234567890"); // Exactly 20 characters
            when(transactionService.createTransaction(any(Transaction.class))).thenReturn(mockSavedDBTransaction);
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isCreated());

            verify(transactionService, times(1)).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should accept minimum valid amount")
        void testCreateTransaction_MinValidAmount() throws Exception {
            validTransaction.setAmount(new BigDecimal("0.01")); // Minimum allowed
            when(transactionService.createTransaction(any(Transaction.class))).thenReturn(mockSavedDBTransaction);
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isCreated());

            verify(transactionService, times(1)).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should reject amount below minimum")
        void testCreateTransaction_AmountBelowMinimum() throws Exception {
            validTransaction.setAmount(new BigDecimal("0.009")); // Below 0.01 minimum
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should reject zero amount")
        void testCreateTransaction_ZeroAmount() throws Exception {
            validTransaction.setAmount(new BigDecimal("0.00"));
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Amount must be equal or greater than 0.01")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when trade number is empty string")
        void testCreateTransaction_EmptyTradeNo() throws Exception {
            validTransaction.setTradeNo(""); // Empty string
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when account name is empty string")
        void testCreateTransaction_EmptyAccountName() throws Exception {
            validTransaction.setAccountName(""); // Empty string
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when currency is empty string")
        void testCreateTransaction_EmptyCurrency() throws Exception {
            validTransaction.setCurrency("");
            
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("Get Transaction by ID Tests")
    class GetTransactionByIdTests {

        @Test
        @DisplayName("Should get transaction successfully with valid ID")
        void testGetTransactionById_Success() throws Exception {
            when(transactionService.getTransactionById(1L)).thenReturn(mockSavedDBTransaction);

            mockMvc.perform(get("/transaction/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.tradeNo").value("123456789012345654"))
                    .andExpect(jsonPath("$.accountName").value("david"));

            verify(transactionService, times(1)).getTransactionById(1L);
        }

        @Test
        @DisplayName("Should return 404 when transaction not found")
        void testGetTransactionById_NotFound() throws Exception {
            when(transactionService.getTransactionById(999L))
                    .thenThrow(new TransactionNotFoundException("Transaction not found with ID: 999"));

            mockMvc.perform(get("/transaction/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Transaction not found with ID: 999")));

            verify(transactionService, times(1)).getTransactionById(999L);
        }

        @Test
        @DisplayName("Should return 400 when ID is invalid")
        void testGetTransactionById_InvalidId() throws Exception {
            mockMvc.perform(get("/transaction/0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Transaction ID must be positive")));

            verify(transactionService, never()).getTransactionById(anyLong());
        }
    }

    @Nested
    @DisplayName("Get Transaction by TradeNo Tests")
    class GetTransactionByTradeNoTests {

        @Test
        @DisplayName("Should get transaction successfully with valid trade number")
        void testGetTransactionByTradeNo_Success() throws Exception {
            when(transactionService.getTransactionByTradeNo("123456789012345654")).thenReturn(mockSavedDBTransaction);

            mockMvc.perform(get("/transaction/by-trade-no/123456789012345654"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tradeNo").value("123456789012345654"))
                    .andExpect(jsonPath("$.accountName").value("david"));

            verify(transactionService, times(1)).getTransactionByTradeNo("123456789012345654");
        }

        @Test
        @DisplayName("Should return 404 when transaction not found by trade number")
        void testGetTransactionByTradeNo_NotFound() throws Exception {
            when(transactionService.getTransactionByTradeNo("999999999999999999"))
                    .thenThrow(new TransactionNotFoundException("Transaction not found with TradeNo: 999999999999999999"));

            mockMvc.perform(get("/transaction/by-trade-no/999999999999999999"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Transaction not found with TradeNo: 999999999999999999")));

            verify(transactionService, times(1)).getTransactionByTradeNo("999999999999999999");
        }

        @Test
        @DisplayName("Should return 400 when trade number format is invalid")
        void testGetTransactionByTradeNo_InvalidFormat() throws Exception {
            mockMvc.perform(get("/transaction/by-trade-no/12345"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Trade number must be 18 digits")));

            verify(transactionService, never()).getTransactionByTradeNo(anyString());
        }

        @Test
        @DisplayName("Should return 400 when trade number contains letters")
        void testGetTransactionByTradeNo_ContainsLetters() throws Exception {
            mockMvc.perform(get("/transaction/by-trade-no/12345678901234567A"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Trade number must be 18 digits")));

            verify(transactionService, never()).getTransactionByTradeNo(anyString());
        }
    }

    @Nested
    @DisplayName("Update Transaction Tests")
    class UpdateTransactionTests {

        @Test
        @DisplayName("Should update transaction successfully by ID")
        void testUpdateTransaction_Success() throws Exception {
            Transaction updatedTransaction = new Transaction();
            updatedTransaction.setId(1L);
            updatedTransaction.setTradeNo("123456789012345654");
            updatedTransaction.setAccountNumber("1234567890123456");
            updatedTransaction.setAccountName("Updated Name");
            updatedTransaction.setAmount(new BigDecimal("2000.00"));
            updatedTransaction.setCurrency("USD");
            updatedTransaction.setType(TransactionType.DEPOSIT);
            updatedTransaction.setDebitCredit(DebitCredit.CREDIT);

            when(transactionService.updateTransaction(eq(1L), any(Transaction.class))).thenReturn(updatedTransaction);

            validTransaction.setAccountName("Updated Name");
            validTransaction.setAmount(new BigDecimal("2000.00"));
            validTransaction.setCurrency("USD");

            mockMvc.perform(put("/transaction/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountName").value("Updated Name"))
                    .andExpect(jsonPath("$.amount").value(2000.00))
                    .andExpect(jsonPath("$.currency").value("USD"));

            verify(transactionService, times(1)).updateTransaction(eq(1L), any(Transaction.class));
        }

        @Test
        @DisplayName("Should update transaction successfully by trade number")
        void testUpdateTransactionByTradeNo_Success() throws Exception {
            when(transactionService.updateTransactionByTradeNo(eq("123456789012345654"), any(Transaction.class)))
                    .thenReturn(mockSavedDBTransaction);

            mockMvc.perform(put("/transaction/by-trade-no/123456789012345654")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tradeNo").value("123456789012345654"));

            verify(transactionService, times(1)).updateTransactionByTradeNo(eq("123456789012345654"), any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent transaction")
        void testUpdateTransaction_NotFound() throws Exception {
            when(transactionService.updateTransaction(eq(999L), any(Transaction.class)))
                    .thenThrow(new TransactionNotFoundException("Transaction not found with ID: 999"));

            mockMvc.perform(put("/transaction/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Transaction not found with ID: 999")));

            verify(transactionService, times(1)).updateTransaction(eq(999L), any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 400 when update data is invalid")
        void testUpdateTransaction_InvalidData() throws Exception {
            validTransaction.setAmount(new BigDecimal("-100"));

            mockMvc.perform(put("/transaction/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Amount must be equal or greater than 0.01")));

            verify(transactionService, never()).updateTransaction(anyLong(), any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("Delete Transaction Tests")
    class DeleteTransactionTests {

        @Test
        @DisplayName("Should delete transaction successfully by ID")
        void testDeleteTransaction_Success() throws Exception {
            doNothing().when(transactionService).deleteTransaction(1L);

            mockMvc.perform(delete("/transaction/1"))
                    .andExpect(status().isNoContent());

            verify(transactionService, times(1)).deleteTransaction(1L);
        }

        @Test
        @DisplayName("Should delete transaction successfully by trade number")
        void testDeleteTransactionByTradeNo_Success() throws Exception {
            doNothing().when(transactionService).deleteTransactionByTradeNo("123456789012345654");

            mockMvc.perform(delete("/transaction/by-trade-no/123456789012345654"))
                    .andExpect(status().isNoContent());

            verify(transactionService, times(1)).deleteTransactionByTradeNo("123456789012345654");
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent transaction")
        void testDeleteTransaction_NotFound() throws Exception {
            doThrow(new TransactionNotFoundException("Transaction not found with ID: 999"))
                    .when(transactionService).deleteTransaction(999L);

            mockMvc.perform(delete("/transaction/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Transaction not found with ID: 999")));

            verify(transactionService, times(1)).deleteTransaction(999L);
        }

        @Test
        @DisplayName("Should return 400 when delete ID is invalid")
        void testDeleteTransaction_InvalidId() throws Exception {
            mockMvc.perform(delete("/transaction/0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Transaction ID must be positive")));

            verify(transactionService, never()).deleteTransaction(anyLong());
        }
    }

    @Nested
    @DisplayName("Get Transactions with Pagination Tests")
    class GetTransactionsTests {

        @Test
        @DisplayName("Should get transactions with default pagination")
        void testGetTransactions_DefaultPagination() throws Exception {
            List<Transaction> transactions = Arrays.asList(mockSavedDBTransaction);
            PagedResult<Transaction> pagedResult = new PagedResult<>(transactions, 0, 20, 1, 1);

            when(transactionService.getTransactions(0, 20)).thenReturn(pagedResult);

            mockMvc.perform(get("/transaction"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.content[0].tradeNo").value("123456789012345654"));

            verify(transactionService, times(1)).getTransactions(0, 20);
        }

        @Test
        @DisplayName("Should get transactions with custom pagination")
        void testGetTransactions_CustomPagination() throws Exception {
            List<Transaction> transactions = Arrays.asList(mockSavedDBTransaction);
            PagedResult<Transaction> pagedResult = new PagedResult<>(transactions, 1, 10, 1, 1);

            when(transactionService.getTransactions(1, 10)).thenReturn(pagedResult);

            mockMvc.perform(get("/transaction")
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(1))
                    .andExpect(jsonPath("$.size").value(10));

            verify(transactionService, times(1)).getTransactions(1, 10);
        }

        @Test
        @DisplayName("Should return 400 when page is negative")
        void testGetTransactions_NegativePage() throws Exception {
            mockMvc.perform(get("/transaction")
                    .param("page", "-1"))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).getTransactions(anyInt(), anyInt());
        }

        @Test
        @DisplayName("Should return 400 when size exceeds maximum")
        void testGetTransactions_ExceedMaxSize() throws Exception {
            mockMvc.perform(get("/transaction")
                    .param("size", "101"))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).getTransactions(anyInt(), anyInt());
        }

        @Test
        @DisplayName("Should return 400 when size is zero")
        void testGetTransactions_ZeroSize() throws Exception {
            mockMvc.perform(get("/transaction")
                    .param("size", "0"))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).getTransactions(anyInt(), anyInt());
        }

        @Test
        @DisplayName("Should return empty result when no transactions found")
        void testGetTransactions_EmptyResult() throws Exception {
            PagedResult<Transaction> emptyResult = new PagedResult<>(Arrays.asList(), 0, 20, 0, 0);

            when(transactionService.getTransactions(0, 20)).thenReturn(emptyResult);

            mockMvc.perform(get("/transaction"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(transactionService, times(1)).getTransactions(0, 20);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle maximum valid amount")
        void testCreateTransaction_MaxValidAmount() throws Exception {
            validTransaction.setAmount(new BigDecimal("99999999999999999.99")); // 17 integer digits, 2 decimals
            when(transactionService.createTransaction(any(Transaction.class))).thenReturn(mockSavedDBTransaction);

            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isCreated());

            verify(transactionService, times(1)).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should handle minimum valid amount")
        void testCreateTransaction_MinValidAmount() throws Exception {
            validTransaction.setAmount(new BigDecimal("0.01"));
            when(transactionService.createTransaction(any(Transaction.class))).thenReturn(mockSavedDBTransaction);

            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isCreated());

            verify(transactionService, times(1)).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should handle maximum account name length")
        void testCreateTransaction_MaxAccountNameLength() throws Exception {
            String maxLengthName = "A".repeat(100);
            validTransaction.setAccountName(maxLengthName);
            when(transactionService.createTransaction(any(Transaction.class))).thenReturn(mockSavedDBTransaction);

            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isCreated());

            verify(transactionService, times(1)).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should handle minimum account name length")
        void testCreateTransaction_MinAccountNameLength() throws Exception {
            validTransaction.setAccountName("AB"); // 2 characters
            when(transactionService.createTransaction(any(Transaction.class))).thenReturn(mockSavedDBTransaction);

            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isCreated());

            verify(transactionService, times(1)).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should handle maximum description length")
        void testCreateTransaction_MaxDescriptionLength() throws Exception {
            String maxDescription = "T".repeat(500);
            validTransaction.setDescription(maxDescription);
            when(transactionService.createTransaction(any(Transaction.class))).thenReturn(mockSavedDBTransaction);

            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isCreated());

            verify(transactionService, times(1)).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should reject description exceeding maximum length")
        void testCreateTransaction_ExceedMaxDescriptionLength() throws Exception {
            String tooLongDescription = "T".repeat(501);
            validTransaction.setDescription(tooLongDescription);

            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Description cannot exceed 500 characters")));

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("Content Type and JSON Handling Tests")
    class ContentTypeTests {

        @Test
        @DisplayName("Should reject request without content type")
        void testCreateTransaction_NoContentType() throws Exception {
            mockMvc.perform(post("/transaction")
                    .content(objectMapper.writeValueAsString(validTransaction)))
                    .andExpect(status().isUnsupportedMediaType());

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should reject malformed JSON")
        void testCreateTransaction_MalformedJson() throws Exception {
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json"))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should handle empty JSON object")
        void testCreateTransaction_EmptyJson() throws Exception {
            mockMvc.perform(post("/transaction")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).createTransaction(any(Transaction.class));
        }
    }
} 