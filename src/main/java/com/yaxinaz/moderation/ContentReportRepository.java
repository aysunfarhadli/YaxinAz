package com.yaxinaz.moderation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {

    Page<ContentReport> findAllByStatus(ReportStatus status, Pageable pageable);

    long countByStatus(ReportStatus status);
}
