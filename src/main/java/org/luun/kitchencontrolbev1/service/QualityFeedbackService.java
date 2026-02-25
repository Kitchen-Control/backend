package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.QualityFeedbackRequest;
import org.luun.kitchencontrolbev1.dto.response.QualityFeedbackResponse;

import java.util.List;

public interface QualityFeedbackService {
    List<QualityFeedbackResponse> getAllFeedbacks();
    QualityFeedbackResponse createFeedback(QualityFeedbackRequest request);
}
