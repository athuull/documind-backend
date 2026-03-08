package com.athul.bhaang.Repository;

import com.athul.bhaang.Entity.Client;
import com.athul.bhaang.Entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);
    List<Vehicle> findByClient(Client client);
}