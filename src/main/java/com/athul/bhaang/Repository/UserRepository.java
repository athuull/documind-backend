package com.athul.bhaang.Repository;

import com.athul.bhaang.Entity.Client;
import com.athul.bhaang.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    @Query("SELECT c FROM Client c JOIN FETCH c.createdBy JOIN FETCH c.assignedTo WHERE c.id = :id")
    Optional<Client> findByIdWithUsers(@Param("id") Long id);
    boolean existsByEmail(String email);


}
