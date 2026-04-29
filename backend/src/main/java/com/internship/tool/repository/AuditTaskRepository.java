package com.internship.tool.repository;

import com.internship.tool.entity.AuditStatus;
import com.internship.tool.entity.AuditTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AuditTaskRepository extends JpaRepository<AuditTask, Long> {

    List<AuditTask> findByAuditProgramId(Long auditProgramId);

    List<AuditTask> findByAssignedToId(Long userId);

    List<AuditTask> findByAuditProgramIdAndStatus(Long auditProgramId, AuditStatus status);

    /** Tasks assigned to a specific auditor that are not yet completed. */
    @Query("""
            SELECT t FROM AuditTask t
            WHERE t.assignedTo.id = :userId
              AND t.status NOT IN ('COMPLETED', 'CANCELLED')
            ORDER BY t.dueDate ASC
            """)
    List<AuditTask> findOpenTasksByUser(@Param("userId") Long userId);

    /** Tasks past their due date and not yet completed. */
    @Query("""
            SELECT t FROM AuditTask t
            WHERE t.dueDate < :today
              AND t.status NOT IN ('COMPLETED', 'CANCELLED')
            """)
    List<AuditTask> findOverdueTasks(@Param("today") LocalDate today);

    /** Used by OverdueTaskScheduler — derived-query equivalent. */
    List<AuditTask> findByDueDateBeforeAndStatusNot(LocalDate date, AuditStatus status);
}
