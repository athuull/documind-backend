package com.athul.documind.Entity;

import com.athul.documind.Entity.Document;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "extracted_data")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExtractedData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String policyNumber;
    private String provider;
    private String vehicleRegistration;
    private String insuredName;

    @Column(columnDefinition = "TEXT")
    private String coverageDetails;

    @OneToOne
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

    @Version
    private Long version;
}