package com.athul.documind.Repository;

import com.athul.documind.Entity.Policy;
import com.athul.documind.Enum.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.athul.documind.Enum.PolicyType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    List<Policy> findByStatus(PolicyStatus status);

    List<Policy> findByClientId(Long clientId);

    Optional<Policy> findByPolicyNumber(String policyNumber);

    boolean existsByPolicyNumber(String policyNumber);

    List<Policy> findByType(PolicyType type);

    List<Policy> findByStatusAndEndDateBetween(PolicyStatus status, LocalDate start, LocalDate end);

    @Query("SELECT p FROM Policy p WHERE LOWER(p.policyNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.insuredName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.provider) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Policy> searchPolicies(@Param("q") String q);

    long countByStatus(PolicyStatus status);
}