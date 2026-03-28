package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.QualityFeedbackRequest;
import org.luun.kitchencontrolbev1.dto.response.QualityFeedbackResponse;
import org.luun.kitchencontrolbev1.service.QualityFeedbackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feedbacks")
@Tag(name = "Quality Feedback API", description = "API for managing customer feedbacks")
public class QualityFeedbackController {

    private final QualityFeedbackService feedbackService;

    @GetMapping
    @Operation(summary = "Get all feedbacks")
    public List<QualityFeedbackResponse> getAllFeedbacks() {
        return feedbackService.getAllFeedbacks();
    }

    @PostMapping
    @Operation(summary = "Create a new feedback for an order")
    public QualityFeedbackResponse createFeedback(@RequestBody QualityFeedbackRequest request) {
        return feedbackService.createFeedback(request);
    }
}
