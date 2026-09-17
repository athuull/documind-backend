package com.athul.documind.Repository;

import com.athul.documind.Entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT d FROM Document d WHERE d.policy.policyId = :policyId")
    List<Document> findByPolicyId(@Param("policyId") Long policyId);

    @Query("SELECT d FROM Document d WHERE d.policy.client.id = :clientId")
    List<Document> findByPolicyClientId(@Param("clientId") Long clientId);
}