package com.jpmc.midascore.service;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
public class IncentiveService {

    private static final String INCENTIVE_API_URL = "http://localhost:8080/incentive";

    @Autowired
    private RestTemplate restTemplate;

    public float getIncentiveAmount(Transaction transaction) {
        try {
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request entity with transaction
            HttpEntity<Transaction> request = new HttpEntity<>(transaction, headers);

            // Make POST request to incentive API
            ResponseEntity<Incentive> response = restTemplate.postForEntity(
                    INCENTIVE_API_URL,
                    request,
                    Incentive.class
            );

            if (response.getBody() != null) {
                float incentiveAmount = response.getBody().getAmount();
                System.out.println("📈 Incentive API response: " + incentiveAmount + " for transaction amount: " + transaction.getAmount());
                return incentiveAmount;
            } else {
                System.out.println("⚠️ Incentive API returned null response");
                return 0.0f;
            }

        } catch (Exception e) {
            System.err.println("❌ Error calling incentive API: " + e.getMessage());
            e.printStackTrace();
            return 0.0f; // Return 0 incentive on error
        }
    }
}