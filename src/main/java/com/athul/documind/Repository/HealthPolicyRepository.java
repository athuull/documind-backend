package com.athul.documind.Repository;

import com.athul.documind.Entity.HealthPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthPolicyRepository extends JpaRepository<HealthPolicy , Long> {
}
