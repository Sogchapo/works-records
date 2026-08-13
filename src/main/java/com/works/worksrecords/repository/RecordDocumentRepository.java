package com.works.worksrecords.repository;

import com.works.worksrecords.model.RecordDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordDocumentRepository extends JpaRepository<RecordDocument, Long> {
}