package com.works.worksrecords.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "works_records")
@Data
public class WorksRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String propertyOwnerName;
    private String applicantName;
    private String contactNumber;
    private String plotNumber;
    private String digitalAddress;
    private String gpsCoordinates;
    private String permitNumber;
    private String buildingPermitNumber;
    private String applicationNumber;
    private String landTitleNumber;

    private BigDecimal amountPaid;

    private String region = "Ashanti Region";
    private String district;
    private String town;
    private String electoralArea;
    private String streetName;

    @Enumerated(EnumType.STRING)
    private DevelopmentType developmentType;

    @Enumerated(EnumType.STRING)
    private RecordStatus status = RecordStatus.PENDING;

    private LocalDate dateSubmitted = LocalDate.now();
    private LocalDate dateApproved;

    @OneToMany(mappedBy = "worksRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecordDocument> documents = new ArrayList<>();

    public enum DevelopmentType {
        RESIDENTIAL, COMMERCIAL, INDUSTRIAL, INSTITUTIONAL, MIXED_USE
    }

    public enum RecordStatus {
        PENDING, APPROVED, REJECTED, UNDER_REVIEW
    }
}