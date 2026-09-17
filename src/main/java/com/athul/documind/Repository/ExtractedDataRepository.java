package com.athul.documind.Repository;


import com.athul.documind.Entity.ExtractedData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExtractedDataRepository extends JpaRepository<ExtractedData, Long> {
}