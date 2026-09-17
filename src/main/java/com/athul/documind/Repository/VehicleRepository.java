package com.athul.documind.Repository;

import com.athul.documind.Entity.Client;
import com.athul.documind.Entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);
    List<Vehicle> findByClient(Client client);
}