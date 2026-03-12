package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LogBatchRepository extends JpaRepository<LogBatch, Integer> {
    List<LogBatch> findByPlan_PlanId(Integer planId);

    List<LogBatch> findByProduct_ProductId(Integer productId);

    @Query(value = "SELECT * FROM log_batches lb WHERE lb.status::text = :status", nativeQuery = true)
    List<LogBatch> findByStatus(@Param("status") String status);

    //    @Query("SELECT lb FROM LogBatch lb WHERE lb.expiryDate < :today AND lb.status IN :statuses")
//    List<LogBatch> findByExpiryDateBeforeAndStatusIn(
//            @Param("today") LocalDate today,
//            @Param("statuses") List<LogBatchStatus> statuses
//    );
    @Query(value = "SELECT * FROM log_batches lb " +
            "JOIN inventories i ON i.batch_id = lb.batch_id " +
            "WHERE lb.expiry_date < :today " +
            "AND lb.status::text IN (:statuses)" +
            "AND i.quantity > 0", nativeQuery = true)
    List<LogBatch> findByExpiryDateBeforeAndStatusIn(
            @Param("today") LocalDate today,
            @Param("statuses") List<String> statuses
    );
}
