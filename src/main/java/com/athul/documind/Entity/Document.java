package com.athul.documind.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String filePath;

    @Column(nullable = false)
    private LocalDateTime uploadDate = LocalDateTime.now();

    private boolean processed;

    private LocalDateTime extractedAt;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @OneToOne(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private ExtractedData extractedData;

    @Version
    private Long version;

    public Document(String text) {
    }
}