package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.Receipt;
import org.luun.kitchencontrolbev1.enums.ReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Integer> {
    List<Receipt> findByOrder_OrderId(Integer orderId);

    List<Receipt> findByStatus(ReceiptStatus status);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Receipt r 
            SET r.status = :newStatus
            WHERE r.receiptId IN :ids
            """)
    void updateReceiptByIds(
            @Param("ids") List<Integer> ids,
            @Param("newStatus") ReceiptStatus newStatus);
}
