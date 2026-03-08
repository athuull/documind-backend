package com.athul.bhaang.Repository;


import com.athul.bhaang.Entity.ExtractedData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExtractedDataRepository extends JpaRepository<ExtractedData, Long> {
}