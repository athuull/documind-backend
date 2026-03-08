package com.athul.bhaang.Repository;

import com.athul.bhaang.Entity.Document;
import com.athul.bhaang.Entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}