package com.athul.bhaang.Repository;

import com.athul.bhaang.Entity.HealthPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthPolicyRepository extends JpaRepository<HealthPolicy , Long> {
}
