package com.athul.bhaang.Service;

import com.athul.bhaang.Entity.Policy;
import com.athul.bhaang.Enum.PolicyStatus;
import com.athul.bhaang.Repository.PolicyRepository;
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

        // 1. Expire ACTIVE policies that have ended
        List<Policy> activePolicies = policyRepository.findByStatus(PolicyStatus.ACTIVE);

        List<Policy> policiesToExpire = activePolicies.stream()
                .filter(p -> p.getEndDate().isBefore(today))
                .peek(p -> p.setStatus(PolicyStatus.EXPIRED))
                .collect(Collectors.toList());

        policyRepository.saveAll(policiesToExpire);

        // 2. Activate CANCELLED policies whose start date is today or before
        List<Policy> cancelledPolicies = policyRepository.findByStatus(PolicyStatus.CANCELLED);

        List<Policy> policiesToActivate = cancelledPolicies.stream()
                .filter(p -> !p.getStartDate().isAfter(today) && p.getEndDate().isAfter(today))
                .peek(p -> p.setStatus(PolicyStatus.ACTIVE))
                .collect(Collectors.toList());

        policyRepository.saveAll(policiesToActivate);

        log.info("Cron Job Finished. Policies Expired: {}. Policies Activated: {}.",
                policiesToExpire.size(), policiesToActivate.size());
    }
}