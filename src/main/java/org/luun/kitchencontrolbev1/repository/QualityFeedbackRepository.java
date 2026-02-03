package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.QualityFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QualityFeedbackRepository extends JpaRepository<QualityFeedback, Integer> {
    List<QualityFeedback> findByStoreStoreId(Integer storeId);
    List<QualityFeedback> findByOrderOrderId(Integer orderId);
}
