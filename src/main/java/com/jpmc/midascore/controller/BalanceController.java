package com.jpmc.midascore.controller;

import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class BalanceController {

    private static final Logger logger = LoggerFactory.getLogger(BalanceController.class);

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/balance")
    public Balance getBalance(@RequestParam("userId") long userId) {
        logger.info("Balance request received for userId: {}", userId);

        try {
            // Get user by ID
            UserRecord user = transactionService.getUserById(userId);

            if (user != null) {
                float userBalance = user.getBalance();
                logger.info("User {} found with balance: {}", userId, userBalance);
                return new Balance(userBalance);
            } else {
                logger.info("User {} not found, returning balance 0", userId);
                return new Balance(0.0f);
            }

        } catch (Exception e) {
            logger.error("Error getting balance for user {}: {}", userId, e.getMessage());
            // Return 0 balance on error as specified
            return new Balance(0.0f);
        }
    }
}