package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.QualityFeedbackRequest;
import org.luun.kitchencontrolbev1.dto.response.QualityFeedbackResponse;
import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.entity.QualityFeedback;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.repository.OrderRepository;
import org.luun.kitchencontrolbev1.repository.QualityFeedbackRepository;
import org.luun.kitchencontrolbev1.service.QualityFeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QualityFeedbackServiceImpl implements QualityFeedbackService {

    private final QualityFeedbackRepository feedbackRepository;
    private final OrderRepository orderRepository;

    @Override
    public List<QualityFeedbackResponse> getAllFeedbacks() {
        return feedbackRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QualityFeedbackResponse createFeedback(QualityFeedbackRequest request) {
        // 1. Validate Order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + request.getOrderId()));

        // 2. Check if feedback already exists for this order
        if (order.getQualityFeedback() != null) {
            throw new RuntimeException("Feedback already exists for this order");
        }

        // 3. Check if order is completed (Optional business rule)
        if (order.getStatus() != OrderStatus.DONE) {
             throw new RuntimeException("Cannot give feedback for incomplete orders");
             // Uncomment above line if you want to restrict feedback to DONE orders only
        }

        // 4. Create Feedback
        QualityFeedback feedback = new QualityFeedback();
        feedback.setOrder(order);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        feedback.setCreatedAt(LocalDateTime.now());

        QualityFeedback savedFeedback = feedbackRepository.save(feedback);
        return mapToResponse(savedFeedback);
    }

    private QualityFeedbackResponse mapToResponse(QualityFeedback feedback) {
        QualityFeedbackResponse response = new QualityFeedbackResponse();
        response.setFeedbackId(feedback.getFeedbackId());
        
        if (feedback.getOrder() != null) {
            response.setOrderId(feedback.getOrder().getOrderId());
        }

        
        response.setRating(feedback.getRating());
        response.setComment(feedback.getComment());
        response.setCreatedAt(feedback.getCreatedAt());
        
        return response;
    }
}
