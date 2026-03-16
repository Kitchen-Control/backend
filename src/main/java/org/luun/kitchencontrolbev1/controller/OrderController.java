package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.ConfirmAllocationRequest;
import org.luun.kitchencontrolbev1.dto.request.OrderRequest;
import org.luun.kitchencontrolbev1.dto.request.OrderStatusUpdateRequest;
import org.luun.kitchencontrolbev1.dto.response.FefoSuggestionResponse;
import org.luun.kitchencontrolbev1.dto.response.OrderResponse;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.service.OrderDetailFillService;
import org.luun.kitchencontrolbev1.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@Tag(name = "Orders API", description = "API for managing orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderDetailFillService orderDetailFillService;

    @GetMapping
    @Operation(summary = "Get all orders")
    public List<OrderResponse> getOrders() {
        return orderService.getOrders();
    }

    @GetMapping("/get-by-store/{storeId}")
    @Operation(summary = "Get orders by store ID")
    public List<OrderResponse> getOrdersByStoreId(@PathVariable Integer storeId) {
        return orderService.getOrdersByStoreId(storeId);
    }

    @GetMapping("/filter-by-status")
    @Operation(summary = "Get orders by status", description = "Get orders by status (WAITTING, PROCESSING, DELIVERING, DISPATCHED, DONE, DAMAGED, CANCLED)")
    public List<OrderResponse> getOrdersByStatus(@RequestParam OrderStatus status) {
        return orderService.getOrdersByStatus(status);
    }

    @GetMapping("/get-by-shipper/{shipperId}")
    @Operation(summary = "Get orders by shipper ID")
    public List<OrderResponse> getOrdersByShipperId(@PathVariable Integer shipperId) {
        return orderService.getOrdersByShipperId(shipperId);
    }

    @PostMapping
    @Operation(summary = "Create a new order")
    public void createOrder(@RequestBody OrderRequest request) {
        orderService.createOrder(request);
    }

    @PatchMapping("/update-status/{storeId}")
    @Operation(summary = "Update order status")
    public void updateOrderStatus(
            @RequestParam Integer orderId,
            @RequestParam("status") OrderStatus status,
            @RequestParam(value = "storeId", required = false) String note
    ) {
        orderService.updateOrderStatus(orderId, status, note);
    }

    @Operation(
            summary = "Lấy đề xuất phân bổ lô hàng theo FEFO",
            description = "Trả về một kế hoạch đề xuất để lấy hàng từ các lô có ngày hết hạn gần nhất. " +
                    "API này KHÔNG thay đổi bất kỳ dữ liệu nào trong cơ sở dữ liệu. " +
                    "Chỉ gọi được khi đơn hàng ở trạng thái 'WAITING'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trả về kế hoạch đề xuất thành công.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FefoSuggestionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy đơn hàng với ID đã cung cấp.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Đơn hàng không ở trạng thái 'WAITING' nên không thể tạo đề xuất.",
                    content = @Content
            )
    })
    @GetMapping("/{orderId}/allocation-suggestion")
    public FefoSuggestionResponse getFefoSuggestion(@PathVariable("orderId") Integer orderId) {
        return orderService.getFefoAllocationSuggestion(orderId);
    }

    @Operation(
            summary = "Xác nhận phân bổ và duyệt đơn hàng",
            description = "Gửi kế hoạch phân bổ cuối cùng (đã được thủ kho xem xét và có thể đã chỉnh sửa) để xác nhận. " +
                    "Hệ thống sẽ thực hiện validation cuối cùng về số lượng và tồn kho. " +
                    "Nếu thành công, các bản ghi 'order_detail_fill' sẽ được tạo và trạng thái đơn hàng sẽ chuyển thành 'PROCESSING'. " +
                    "Toàn bộ quá trình được thực hiện trong một giao dịch duy nhất."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Xác nhận phân bổ và duyệt đơn thành công. Trạng thái đơn hàng đã được cập nhật."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Yêu cầu không hợp lệ. Có thể do: \n" +
                            "1. Tổng số lượng phân bổ không khớp với yêu cầu của đơn hàng.\n" +
                            "2. Tồn kho của một lô nào đó không đủ tại thời điểm xác nhận (Race Condition).\n" +
                            "3. Đơn hàng không ở trạng thái 'WAITING'.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy đơn hàng hoặc một trong các tài nguyên liên quan (sản phẩm, lô hàng).",
                    content = @Content
            )
    })
    @PostMapping("/{orderId}/confirm-allocation")
    public void confirmAllocation(
            @PathVariable("orderId") Integer orderId,
            @RequestBody ConfirmAllocationRequest request) {
        orderService.confirmAllocation(orderId, request);
    }
}
