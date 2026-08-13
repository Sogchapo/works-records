package com.works.worksrecords.repository;

import com.works.worksrecords.model.WorksRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorksRecordRepository extends JpaRepository<WorksRecord, Long> {

    long countByDateSubmitted(LocalDate date);
    long countByStatus(WorksRecord.RecordStatus status);

    List<WorksRecord> findTop5ByOrderByIdDesc();

    @Query("SELECT r FROM WorksRecord r WHERE " +
           "LOWER(r.propertyOwnerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.plotNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.permitNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.digitalAddress) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.town) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.contactNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.applicationNumber) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<WorksRecord> searchRecords(@Param("query") String query);

    List<WorksRecord> findByStatus(WorksRecord.RecordStatus status);

    List<WorksRecord> findByDateSubmittedBetween(LocalDate startDate, LocalDate endDate);
}