package com.athul.bhaang.Repository;

import com.athul.bhaang.Entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByCreatedById(Long userId);
}
