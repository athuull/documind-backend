package com.athul.documind.Service;

import com.athul.documind.Entity.Policy;
import com.athul.documind.Enum.PolicyStatus;
import com.athul.documind.Repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyExpiryScheduler {

    private final PolicyRepository policyRepository;

    /**
     * Runs every day at 12:01 AM (Cron: "0 1 0 * * *")
     */
    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void updatePolicyStatuses() {
        LocalDate today = LocalDate.now();
        log.info("Starting scheduled job: Policy Expiry Check - {}", today);

        // 1. Expire ACTIVE policies whose end date has passed
        List<Policy> activePolicies = policyRepository.findByStatus(PolicyStatus.ACTIVE);

        List<Policy> policiesToExpire = activePolicies.stream()
                .filter(p -> p.getEndDate() != null && p.getEndDate().isBefore(today))
                .peek(p -> {
                    p.setStatus(PolicyStatus.EXPIRED);
                    log.info("Policy {} (ID: {}) expired on {}", p.getPolicyNumber(), p.getPolicyId(), p.getEndDate());
                })
                .collect(Collectors.toList());

        if (!policiesToExpire.isEmpty()) {
            policyRepository.saveAll(policiesToExpire);
        }

        // 2. Log policies expiring in the upcoming 7 days for agent awareness
        LocalDate upcomingWeek = today.plusDays(7);
        long upcomingCount = activePolicies.stream()
                .filter(p -> p.getEndDate() != null && !p.getEndDate().isBefore(today) && !p.getEndDate().isAfter(upcomingWeek))
                .count();

        log.info("Policy Expiry Check finished. Expired: {}. Upcoming renewals (next 7 days): {}.",
                policiesToExpire.size(), upcomingCount);
    }
}