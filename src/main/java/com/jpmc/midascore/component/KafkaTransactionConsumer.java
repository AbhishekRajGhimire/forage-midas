package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.service.TransactionService;
import com.jpmc.midascore.entity.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class KafkaTransactionConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaTransactionConsumer.class);
    private int transactionCount = 0;
    // Add this method to your consumer class
    private void printFinalBalances() {
        try {
            UserRecord wilbur = transactionService.getUserByName("wilbur");
            if (wilbur != null) {
                System.out.println("🏁 FINAL WILBUR BALANCE: " + wilbur.getBalance());
                System.out.println("🏁 WILBUR ROUNDED DOWN: " + (int)Math.floor(wilbur.getBalance()));
            }
        } catch (Exception e) {
            System.out.println("Error getting final balance: " + e.getMessage());
        }
    }

    @Autowired
    private TransactionService transactionService;

    @KafkaListener(topics = "${general.kafka-topic}")
    public void handleTransaction(Transaction transaction) {
        transactionCount++;

        // Print the transaction details clearly
        System.out.println("=== RECEIVED TRANSACTION #" + transactionCount + " ===");
        System.out.println("AMOUNT: " + transaction.getAmount());
        System.out.println("Sender: " + transaction.getSenderId() + " -> Recipient: " + transaction.getRecipientId());
        System.out.println("==========================================");

        // Highlight first 4 amounts
        if (transactionCount <= 4) {
            System.err.println("*** TRANSACTION " + transactionCount + " AMOUNT: " + transaction.getAmount() + " ***");
        }
        // Add this at the very end of handleTransaction method
        if (transactionCount == 100) { // Assuming 100 transactions, adjust as needed
            printFinalBalances();
        }

        // Process the transaction with database validation and incentives
        boolean success = transactionService.processTransaction(transaction);

        if (success) {
            System.out.println("✅ Transaction processed successfully with incentives");
        } else {
            System.out.println("❌ Transaction discarded (validation failed)");
        }

        // **DEBUG: Print WILBUR's balance after each transaction** (CHANGED from waldorf to wilbur)
        // 🔍 PUT YOUR BREAKPOINT ON THE LINE BELOW (the try line)
        try {
            UserRecord wilbur = transactionService.getUserByName("wilbur");
            if (wilbur != null) {
                float currentBalance = wilbur.getBalance();
                int roundedBalance = (int)Math.floor(currentBalance);

                System.out.println("🔍 DEBUG: Wilbur's current balance: " + currentBalance);
                System.out.println("🔍 DEBUG: Wilbur's balance (rounded down): " + roundedBalance);

                // **PUT ANOTHER BREAKPOINT HERE** - This is where you can inspect the final balance
                if (transactionCount % 10 == 0) { // Every 10 transactions, print a summary
                    System.out.println("📊 SUMMARY after " + transactionCount + " transactions:");
                    System.out.println("📊 Wilbur balance: " + currentBalance + " (rounded: " + roundedBalance + ")");
                }
            } else {
                System.out.println("⚠️ DEBUG: Wilbur user not found");
            }
        } catch (Exception e) {
            System.out.println("❌ DEBUG: Error getting wilbur balance: " + e.getMessage());
            e.printStackTrace();
        }


        logger.info("Processed transaction #{} with amount: {}", transactionCount, transaction.getAmount());
    }
}