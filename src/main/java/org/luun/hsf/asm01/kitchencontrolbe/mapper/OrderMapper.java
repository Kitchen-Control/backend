package org.luun.hsf.asm01.kitchencontrolbe.mapper;

import org.luun.hsf.asm01.kitchencontrolbe.dto.request.OrderRequestDTO;
import org.luun.hsf.asm01.kitchencontrolbe.dto.response.OrderResponseDTO;
import org.luun.hsf.asm01.kitchencontrolbe.entity.Order;
import org.luun.hsf.asm01.kitchencontrolbe.entity.OrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "deliveryId", ignore = true)
    @Mapping(target = "planId", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "orderDetails", source = "orderDetails")
    Order toEntity(OrderRequestDTO request);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "orderDetailId", ignore = true)
    OrderDetail toOrderDetailEntity(OrderRequestDTO.OrderDetailRequest request);

    OrderResponseDTO toResponseDTO(Order order);

    OrderResponseDTO.OrderDetailResponse toOrderDetailResponse(OrderDetail orderDetail);
}
