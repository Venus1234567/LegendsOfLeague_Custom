package com.project.legendsofleague.domain.order.dto;

import com.project.legendsofleague.domain.membercoupon.dto.MemberCouponResponseDto;
import com.project.legendsofleague.domain.order.domain.Order;
import com.project.legendsofleague.domain.order.domain.OrderItem;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 장바구니 or 상세 아이템에서 주문하기 버튼을 눌렀을 때, 주문하기 전 단계 (쿠폰 등 체크하는 페이지를 위한 dto)
 */
@Getter
@Slf4j
public class OrderResponseDto {

    private Long id;

    private List<OrderItemResponseDto> orderItemList = new ArrayList<>();

    public static OrderResponseDto toDto(List<OrderItem> orderItems,
                                         Map<Long, List<MemberCouponResponseDto>> couponResponseList) {
        OrderResponseDto orderResponseDto = new OrderResponseDto();

        Order order = orderItems.get(0).getOrder();

        orderResponseDto.id = order.getId();
        orderResponseDto.orderItemList = orderItems
                .stream().map((oi) -> OrderItemResponseDto.toDto(oi, couponResponseList)).toList();

        return orderResponseDto;
    }


    public static OrderResponseDto toDto(Order order) {
        OrderResponseDto orderResponseDto = new OrderResponseDto();

        orderResponseDto.id = order.getId();
        orderResponseDto.orderItemList = order.getOrderItemList()
                .stream().map(OrderItemResponseDto::toDto).toList();

        return orderResponseDto;
    }
}
