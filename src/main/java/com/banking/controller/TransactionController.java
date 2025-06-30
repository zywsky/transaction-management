package com.banking.controller;

import com.banking.dto.PagedResult;
import com.banking.model.Transaction;
import com.banking.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/**
 * Transaction Management Controller </br>
 * Exposed APIs to operate transactions, including create, query, update, delete operations. </br>
 * Note: in real transaction system, we may only allow user only operate their own transactions. </br>
 * Since this homework don't required, I did not add the logic.
 */
@RestController
@RequestMapping("/transaction")
@Validated
@Tag(name = "Transaction Management", description = "Banking transaction management")
public class TransactionController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    /**
     * Create a new transaction
     */
    @PostMapping
    @Operation(
        summary = " Create New Transaction",
        description = "Create a new banking transaction"
    )
    @ApiResponses(value = {
        @ApiResponse( responseCode = "201", description = "Transaction created successfully",
                content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Transaction.class)
            )
        ),
        @ApiResponse( responseCode = "400", description = "Invalid request parameters",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\": \"Validation failed\", \"message\": \"Invalid request parameter\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<Transaction> createTransaction(
            @Valid @RequestBody Transaction transaction) {
        logger.info("Creating transaction, transaction number {}", transaction.getTradeNo());
        Transaction savedTransaction = transactionService.createTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
    }

    /**
     * Get transaction by ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Query Transaction by ID", 
        description = "Query transaction record detail by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Query successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Transaction.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Transaction not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\": \"Transaction not found\", \"message\": \"Transaction with ID 123456789012345654 does not exist\"}"
                )
            )
        )
    })
    public ResponseEntity<Transaction> getTransactionById(
            @Parameter(
                description = "Transaction ID, must be a positive integer. It should exist in H2 Database. If not exist, you need first create transaction records.",
                required = true,
                example = "1"
            )
            @PathVariable @Min(value = 1, message = "Transaction ID must be positive") Long id) {

        logger.info("Querying transaction by ID {}", id);
        Transaction transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    /**
     * Get transaction by TradeNo
     */
    @GetMapping("/by-trade-no/{tradeNo}")
    @Operation(
        summary = "Query Transaction by trade number",
        description = "Query transaction detail by 18-digit trade number - business interface"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Query successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Transaction.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Transaction not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\": \"Transaction not found\", \"message\": \"Transaction with TradeNo 123456789012345654 does not exist\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid trade number format",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\": \"Validation failed\", \"message\": \"Trade number must be 18 digits\"}"
                )
            )
        )
    })
    public ResponseEntity<Transaction> getTransactionByTradeNo(
            @Parameter(
                description = "18-digit trade number", 
                required = true,
                example = "123456789012345654"
            )
            @PathVariable 
            @Pattern(regexp = "^\\d{18}$", message = "Trade number must be 18 digits") 
            String tradeNo) {

        logger.info("Querying transaction by tradeNo: {}", tradeNo);
        Transaction transaction = transactionService.getTransactionByTradeNo(tradeNo);
        return ResponseEntity.ok(transaction);
    }

    /**
     * Update transaction
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update Transaction Information", 
        description = "Update existing transaction record information by transaction ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Update successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Transaction.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Transaction not found"
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
        )
    })
    public ResponseEntity<Transaction> updateTransaction(
            @Parameter(
                description = "Transaction ID to update", 
                required = true,
                example = "1"
            )
            @PathVariable @Min(value = 1, message = "Transaction ID must be positive") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Update transaction",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = Transaction.class),
                    examples = @ExampleObject(
                        name = "Update transaction info",
                        value = """
                        {
                          "tradeNo": "123456789012345654",
                          "accountNumber": "1234567890123456",
                          "accountName": "user 1",
                          "payeeAccount": "9876543210987654",
                          "payeeName": "user 2",
                          "amount": 2000.00,
                          "currency": "CNY",
                          "type": "TO",
                          "debitCredit": "DR",
                          "description": ""
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody Transaction transaction) {
        
        logger.info("Updating transaction, transaction id {}, trade number {} ", id, transaction.getTradeNo());
        if (transaction.getId() != null && !transaction.getId().equals(id)) {
            throw new IllegalArgumentException("Transaction ID in request body does not match path variable");
        }

        transaction.setId(id);
        Transaction updatedTransaction = transactionService.updateTransaction(id, transaction);
        return ResponseEntity.ok(updatedTransaction);
    }

    /**
     * Update transaction by TradeNo
     */
    @PutMapping("/by-trade-no/{tradeNo}")
    @Operation(
        summary = "Update Transaction by trade number",
        description = "Update transaction information by 18-digit trade number."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Update successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Transaction.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Transaction not found"
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
        )
    })
    public ResponseEntity<Transaction> updateTransactionByTradeNo(
            @Parameter(
                description = "Trade number",
                required = true,
                example = "123456789012345654"
            )
            @PathVariable 
            @Pattern(regexp = "^\\d{18}$", message = "Trade number must be 18 digits") 
            String tradeNo,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated transaction information by trade number",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = Transaction.class),
                    examples = @ExampleObject(
                        name = "Update Transaction by trade number",
                        value = """
                        {
                          "accountNumber": "1234567890123456",
                          "accountName": "user 1",
                          "payeeAccount": "9876543210987654",
                          "payeeName": "user 2",
                          "amount": 2500.00,
                          "currency": "CNY",
                          "type": "TO",
                          "debitCredit": "DR",
                          "description": ""
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody Transaction transaction) {
        
        logger.info("Updating transaction by tradeNo {}", tradeNo);
        Transaction updatedTransaction = transactionService.updateTransactionByTradeNo(tradeNo, transaction);
        return ResponseEntity.ok(updatedTransaction);
    }

    /**
     * Delete transaction
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete Transaction by ID",
        description = "Delete transaction record by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "Delete successful"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Transaction not found"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid parameter"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(
                description = "Transaction ID to delete", 
                required = true,
                example = "1"
            )
            @PathVariable @Min(value = 1, message = "Transaction ID must be positive") Long id) {
        
        logger.info("Deleting transaction by ID {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete transaction by TradeNo
     */
    @DeleteMapping("/by-trade-no/{tradeNo}")
    @Operation(
        summary = "Delete Transaction by trade number",
        description = "Delete transaction record by 18-digit trade number"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "Delete successful"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Transaction not found"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid trade number format"
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
        )
    })
    public ResponseEntity<Void> deleteTransactionByTradeNo(
            @Parameter(
                description = "Trade number to delete", 
                required = true,
                example = "123456789012345654"
            )
            @PathVariable 
            @Pattern(regexp = "^\\d{18}$", message = "Trade number must be 18 digits") 
            String tradeNo) {

        logger.info("Deleting transaction by trade number {}", tradeNo);
        transactionService.deleteTransactionByTradeNo(tradeNo);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all transactions with pagination
     */
    @GetMapping
    @Operation(
        summary = "Query transaction list with pagination",
        description = "Get paginated list of all transaction records. Sorted by creation time in descending order"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Query successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PagedResult.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "content": [...],
                      "page": 0,
                      "size": 20,
                      "totalElements": 100,
                      "totalPages": 5
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<PagedResult<Transaction>> getTransactions(
            @Parameter(
                description = "Page number, starting from 0", 
                example = "0"
            )
            @RequestParam(defaultValue = "0") @Min(value = 0) int page,
            @Parameter(
                description = "Page size, maximum 100", 
                example = "20"
            )
            @RequestParam(defaultValue = "20") @Min(value = 1) @Max(value = 100) int size) {
        
        logger.info("Fetching transactions, page: {}, size: {}", page, size);
        PagedResult<Transaction> result = transactionService.getTransactions(page, size);
        return ResponseEntity.ok(result);
    }
} 