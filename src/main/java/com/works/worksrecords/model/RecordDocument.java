package com.works.worksrecords.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "record_documents")
@Data
public class RecordDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;
    private String documentCategory; // e.g., Site Plan, Building Plan, Permit
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "works_record_id")
    private WorksRecord worksRecord;
}