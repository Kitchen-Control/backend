package org.luun.kitchencontrolbev1.service.impl;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.OrderDetailFillResponse;
import org.luun.kitchencontrolbev1.entity.OrderDetailFill;
import org.luun.kitchencontrolbev1.repository.OrderDetailFillRepository;
import org.luun.kitchencontrolbev1.service.OrderDetailFillService;
import org.springframework.stereotype.Service;

import org.luun.kitchencontrolbev1.entity.Order;
import org.luun.kitchencontrolbev1.entity.OrderDetail;
import org.luun.kitchencontrolbev1.entity.Inventory;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import org.luun.kitchencontrolbev1.repository.OrderRepository;
import org.luun.kitchencontrolbev1.repository.InventoryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderDetailFillServiceImpl implements OrderDetailFillService {

    private final OrderDetailFillRepository orderDetailFillRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public List<OrderDetailFillResponse> getAllOrderDetailFills() {
        return orderDetailFillRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDetailFillResponse getOrderDetailFillById(Integer fillId) {
        OrderDetailFill fill = orderDetailFillRepository.findById(fillId)
                .orElseThrow(() -> new RuntimeException("OrderDetailFill not found with id: " + fillId));
        return mapToResponse(fill);
    }

    @Override
    public List<OrderDetailFillResponse> getOrderDetailFillsByOrderDetailId(Integer orderDetailId) {
        return orderDetailFillRepository.findByOrderDetail_OrderDetailId(orderDetailId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDetailFillResponse> getOrderDetailFillsByBatchId(Integer batchId) {
        return orderDetailFillRepository.findByBatch_BatchId(batchId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    // Giai đoạn 3.1: Phân bổ tự động (Allocation - Logic FEFO)
    // Hệ thống duyệt từng chi tiết đơn hàng (OrderDetail) để giữ chỗ trong các Lô
    // hàng (Batch) theo nguyên tắc Hạn dùng gần nhất (FEFO).
    public void autoAllocateFEFO(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            return;
        }

        // Chỉ xem xét các đơn hàng đang ở trạng thái WAITTING hoặc PROCESSING để tính
        // toán số lượng "Đã giữ chỗ"
        List<OrderStatus> statuses = Arrays.asList(OrderStatus.WAITTING, OrderStatus.PROCESSING);

        // Duyệt qua từng món hàng mà cửa hàng đặt (OrderDetail)
        for (OrderDetail detail : order.getOrderDetails()) {
            Float neededQuantity = detail.getQuantity(); // Số lượng cửa hàng cần lấy

            // Bước 1: Quét bảng inventories. Tìm các batch của món hàng này có hạn sử dụng
            // >= Hôm nay & Tồn kho thực tế > 0.
            // Danh sách trả về ĐÃ ĐƯỢC SẮP XẾP TĂNG DẦN THEO HẠN SỬ DỤNG (FEFO - Thằng nào
            // sắp hỏng thì lấy trước)
            List<Inventory> inventories = inventoryRepository
                    .findValidInventoriesForProductOrderByExpiryDateAsc(detail.getProduct().getProductId());

            // Bước 2: Duyệt từng lô hàng (Batch) thỏa mãn điều kiện phía trên
            for (Inventory inv : inventories) {
                // Nếu đã gom đủ số lượng cần thiết thì thoát vòng lặp, chuyển sang món hàng
                // tiếp theo của Order
                if (neededQuantity <= 0)
                    break;

                Integer batchId = inv.getBatch().getBatchId();

                // Tính xem Lô này đã bị "giữ chỗ" bao nhiêu hàng cho các Order đang chờ/xử lý
                // khác
                Float reserved = orderDetailFillRepository.getTotalReservedQuantityByBatchId(batchId, statuses);
                if (reserved == null)
                    reserved = 0f;

                // Tồn kho CÒN THẬT SỰ RẢNH RỖI (Available In Batch) = Tồn kho gốc (trong DB) -
                // Số lượng đã bị giữ chỗ
                Float availableInBatch = inv.getQuantity() - reserved;

                // Nếu Lô này vẫn còn trống hàng
                if (availableInBatch > 0) {
                    // Cắt lấy số lượng Tối đa có thể từ Lô này (Nếu cần ít hơn available thì lấy số
                    // cần, nếu cần nhiều hơn thì vét sạch lô này)
                    Float take = Math.min(availableInBatch, neededQuantity);

                    // Giai đoạn 3.1: Ghi vào bảng order_detail_fill. Lúc này Kho thực tế
                    // (inventories) CHƯA BỊ TRỪ, mới chỉ là "giữ chỗ"
                    OrderDetailFill fill = new OrderDetailFill();
                    fill.setOrderDetail(detail);
                    fill.setBatch(inv.getBatch());
                    fill.setQuantity(take);
                    fill.setCreatedAt(LocalDateTime.now());

                    orderDetailFillRepository.save(fill);

                    // Trừ đi số lượng đã nhặt được để tiếp tục đi tìm Lô khác cho đến khi gom đủ
                    neededQuantity -= take;
                }
            }

            // Nếu đã quét sạch sành sanh các Lô trong kho mà vẫn Không gom đủ hàng
            // (neededQuantity > 0)
            // Lập tức Báo Lỗi để dừng lại việc Gom Đơn/Xuất kho (Giao dịch DB sẽ tự
            // Rollback)
            if (neededQuantity > 0) {
                throw new RuntimeException("Not enough available stock to fulfill order for product: "
                        + detail.getProduct().getProductName());
            }
        }
    }

    private OrderDetailFillResponse mapToResponse(OrderDetailFill fill) {
        OrderDetailFillResponse response = new OrderDetailFillResponse();
        response.setFillId(fill.getFillId());

        if (fill.getOrderDetail() != null) {
            response.setOrderDetailId(fill.getOrderDetail().getOrderDetailId());
        }

        if (fill.getBatch() != null) {
            response.setBatchId(fill.getBatch().getBatchId());
        }

        response.setQuantity(fill.getQuantity());
        response.setCreatedAt(fill.getCreatedAt());

        return response;
    }
}
