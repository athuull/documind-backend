package com.athul.bhaang.Repository;

import com.athul.bhaang.Entity.Policy;
import com.athul.bhaang.Enum.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    // Expired policies

    // Policies by status
    List<Policy> findByStatus(PolicyStatus status);

    List<Policy> findByClientId(Long clientId);

    Optional<Policy> findByPolicyNumber(String policyNumber);


}