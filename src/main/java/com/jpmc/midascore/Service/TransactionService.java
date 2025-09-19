package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // NEW: Add IncentiveService dependency
    @Autowired
    private IncentiveService incentiveService;

    @Transactional
    public boolean processTransaction(Transaction transaction) {
        logger.info("Processing transaction: sender={}, recipient={}, amount={}",
                transaction.getSenderId(), transaction.getRecipientId(), transaction.getAmount());

        // Step 1: Validate sender exists
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        if (sender == null) {
            logger.warn("Transaction rejected: Invalid sender ID {}", transaction.getSenderId());
            return false;
        }

        // Step 2: Validate recipient exists
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());
        if (recipient == null) {
            logger.warn("Transaction rejected: Invalid recipient ID {}", transaction.getRecipientId());
            return false;
        }

        // Step 3: Validate sender has sufficient balance
        if (sender.getBalance() < transaction.getAmount()) {
            logger.warn("Transaction rejected: Insufficient balance. Sender {} has balance {}, trying to send {}",
                    sender.getName(), sender.getBalance(), transaction.getAmount());
            return false;
        }

        // NEW: Step 4: Get incentive amount from API (after validation)
        float incentiveAmount = incentiveService.getIncentiveAmount(transaction);
        logger.info("Incentive amount received: {} for transaction amount: {}", incentiveAmount, transaction.getAmount());

        // Step 5: Process the transaction
        // Deduct from sender (no incentive deduction)
        sender.setBalance(sender.getBalance() - transaction.getAmount());

        // Add to recipient (transaction amount + incentive)
        recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

        // Save updated user balances
        userRepository.save(sender);
        userRepository.save(recipient);

        // NEW: Create and save transaction record WITH INCENTIVE
        TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, transaction.getAmount(), incentiveAmount);
        transactionRepository.save(transactionRecord);

        logger.info("Transaction processed successfully: {} -> {} amount: {}, incentive: {}",
                sender.getName(), recipient.getName(), transaction.getAmount(), incentiveAmount);

        // Add this at the very end of the processTransaction method, right before "return true;"
        if (recipient.getName().equals("wilbur")) {
            System.out.println("🎯 WILBUR UPDATE: New balance = " + recipient.getBalance() +
                    " (rounded down: " + (int)Math.floor(recipient.getBalance()) + ")");
        }
        return true;
    }

    public UserRecord getUserByName(String name) {
        return userRepository.findByName(name);
    }

    public void printWaldorfBalance() {
        UserRecord waldorf = getUserByName("waldorf");
        if (waldorf != null) {
            System.out.println("=== FINAL WALDORF BALANCE ===");
            System.out.println("Exact balance: " + waldorf.getBalance());
            System.out.println("Rounded down: " + (int)Math.floor(waldorf.getBalance()));
            System.out.println("===========================");
        }
    }

    // NEW: Add method to print Wilbur's balance for Task 4
    public void printWilburBalance() {
        UserRecord wilbur = getUserByName("wilbur");
        if (wilbur != null) {
            System.out.println("=== FINAL WILBUR BALANCE ===");
            System.out.println("Exact balance: " + wilbur.getBalance());
            System.out.println("Rounded down: " + (int)Math.floor(wilbur.getBalance()));
            System.out.println("===========================");
        }
    }
    // Add this method to your TransactionService.java if it's not already there:

    public UserRecord getUserById(long id) {
        return userRepository.findById(id);
    }

}