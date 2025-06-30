package com.banking.stresstest;

import com.banking.enums.DebitCredit;
import com.banking.enums.TransactionType;
import com.banking.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Stress test class to test creating transactions concurrently
 */
public class StressTest {
    private static final Logger logger = LoggerFactory.getLogger(StressTest.class);

    // the target URL to test
    private static final String URL = "http://localhost:8080/banking/transaction";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final HttpHeaders headers;

    private static final AtomicLong TRANSACTION_SEQUENCE = new AtomicLong(0);
    private static final AtomicReference<Long> BASE_TIMESTAMP = new AtomicReference<>(System.currentTimeMillis());

    public StressTest() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        
        this.headers = new HttpHeaders();
        this.headers.setContentType(MediaType.APPLICATION_JSON);
    }

    public static void main(String[] args) {
        StressTest stressTest = new StressTest();

        logger.info("Starting stress test, target endpoint to test: " + URL);

        int threadCount = 20;
        int transactionsPerThread = 5000;

        logger.info("Test parameters - Thread count: {}, Transactions per thread: {}, Total transactions to be created: {}", threadCount, transactionsPerThread, (threadCount * transactionsPerThread));
        
        try {
            stressTest.startTest(threadCount, transactionsPerThread);
            logger.info("Test completed");
            
        } catch (Exception e) {
            logger.error("Test failed, error: ", e);
            System.exit(1);
        }
    }

    /**
     * start load test
     */
    public void startTest(int threadCount, int transactionsPerThread) throws Exception {
        logger.info("Starting concurrent transaction creation stress test...");

        int totalTransactions = threadCount * transactionsPerThread;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);

        long testStartTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    for (int j = 0; j < transactionsPerThread; j++) {
                        long requestStartTime = System.currentTimeMillis();
                        try {
                            Transaction transaction = createTestTransaction(threadId, j);
                            HttpEntity<String> httpRequest = new HttpEntity<>(
                                objectMapper.writeValueAsString(transaction), headers);
                            
                            ResponseEntity<String> response = restTemplate.postForEntity(
                                    URL, httpRequest, String.class);

                            if (response.getStatusCode() == HttpStatus.CREATED) {
                                long responseTime = System.currentTimeMillis() - requestStartTime;
                                totalResponseTime.addAndGet(responseTime);
                                successCount.incrementAndGet();
                            } else {
                                logger.warn("Thread {} transaction {} returned status code: {}", threadId, j, response.getStatusCode());
                                failureCount.incrementAndGet();
                            }

                        }  catch (Exception e) {
                            logger.error("Thread {} transaction {} failed: {}", threadId, j, e.getMessage());
                            failureCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // Start test
        logger.info("Starting {} threads, each creating {} transactions, total {} transactions\n", threadCount, transactionsPerThread, totalTransactions);
        startLatch.countDown();
        endLatch.await();

        long testEndTime = System.currentTimeMillis();
        long totalTestTime = testEndTime - testStartTime;

        // calculate actual counts
        int actualSuccessCount = successCount.get();
        int actualFailureCount = failureCount.get();
        int actualDuplicateCount = duplicateCount.get();
        
        double successRate = (double) actualSuccessCount / totalTransactions * 100;
        double avgResponseTime = actualSuccessCount > 0 ? (double) totalResponseTime.get() / actualSuccessCount : 0;
        double throughput = actualSuccessCount > 0 ? (double) actualSuccessCount / (totalTestTime / 1000.0) : 0;

        // result
        logger.info("=== Concurrent Stress Test Results ===");
        logger.info("Total transactions:    {}", totalTransactions);
        logger.info("Successful:            {}", actualSuccessCount);
        logger.info("Failed:                {}", actualFailureCount);
        logger.info("Duplicates:            {}", actualDuplicateCount);
        logger.info("Success rate:          {}%", String.format("%.2f",successRate));
        logger.info("Total time:            {} ms", totalTestTime);
        logger.info("Average response time: {} ms\n", String.format("%.2f",avgResponseTime));
        logger.info("Throughput:            {} transactions/sec\n", String.format("%.2f",throughput));
        executor.shutdown();
    }
    
    private Transaction createTestTransaction(int threadId, int transactionIndex) {
        Transaction transaction = new Transaction();
        transaction.setTradeNo(generateUniqueTradeNo());
        long globalIndex = TRANSACTION_SEQUENCE.get();
        transaction.setAccountNumber(String.format("%010d", (globalIndex % 10000000000L + 1000000000L)));
        transaction.setAccountName(generateAccountName(threadId));
        transaction.setPayeeAccount(String.format("%010d", (globalIndex % 10000000000L + 2000000000L)));
        transaction.setPayeeName(generatePayeeName(threadId));
        transaction.setAmount(new BigDecimal("100.00").add(new BigDecimal(globalIndex % 1000)));
        transaction.setCurrency("CNY");
        transaction.setType(TransactionType.TRANSFER_OUT);
        transaction.setDebitCredit(DebitCredit.DEBIT);
        transaction.setDescription("Concurrent stress test " + threadId + "_" + transactionIndex);
        return transaction;
    }
    
    private String generateAccountName(int threadId) {
        String[] prefixes = {"Test", "Demo", "Sample", "Mock"};
        String[] suffixes = {"Account", "User", "Client", "Holder"};
        String prefix = prefixes[threadId % prefixes.length];
        String suffix = suffixes[(threadId / prefixes.length) % suffixes.length];
        return prefix + " " + suffix;
    }
    
    private String generatePayeeName(int threadId) {
        String[] firstNames = {"John", "Jane", "Bob", "Alice", "Nancy", "Frank", "Grace"};
        String[] lastNames = {"Smith", "Johnson", "Brown", "Taylor"};
        String firstName = firstNames[threadId % firstNames.length];
        String lastName = lastNames[(threadId / firstNames.length) % lastNames.length];
        return firstName + " " + lastName;
    }
    private String generateUniqueTradeNo() {
        long sequence = TRANSACTION_SEQUENCE.incrementAndGet();
        if (sequence > 1000) {
            synchronized (BASE_TIMESTAMP) {
                long currentSequence = TRANSACTION_SEQUENCE.get();
                if (currentSequence > 1000) {
                    BASE_TIMESTAMP.set(System.currentTimeMillis());
                    TRANSACTION_SEQUENCE.set(1);
                }
                sequence = TRANSACTION_SEQUENCE.incrementAndGet();
            }
        }
        long baseTime = System.currentTimeMillis();
        long truncatedTime = baseTime % 10000000000000L;
        return String.format("%013d%05d", truncatedTime, sequence);
    }
} 