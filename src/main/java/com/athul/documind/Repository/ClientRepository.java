package com.athul.documind.Repository;

import com.athul.documind.Entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByCreatedById(Long userId);

    List<Client> findByAssignedToId(Long userId);

    @Query("SELECT c FROM Client c LEFT JOIN FETCH c.createdBy LEFT JOIN FETCH c.assignedTo WHERE c.id = :id")
    Optional<Client> findByIdWithUsers(@Param("id") Long id);

    @Query("SELECT c FROM Client c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%')) OR c.phone LIKE CONCAT('%', :q, '%')")
    List<Client> searchClients(@Param("q") String q);
}
