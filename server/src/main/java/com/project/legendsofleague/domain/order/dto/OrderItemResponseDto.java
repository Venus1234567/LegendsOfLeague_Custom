package com.project.legendsofleague.domain.order.dto;

import com.project.legendsofleague.domain.item.domain.Item;
import com.project.legendsofleague.domain.membercoupon.dto.MemberCouponResponseDto;
import com.project.legendsofleague.domain.order.domain.OrderItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class OrderItemResponseDto {

    private Long id;

    private String name;

    private Integer price;

    private Integer count;

    private String category;

    private String thumbnailImage;

    private List<MemberCouponResponseDto> couponList = new ArrayList<>();

    public static OrderItemResponseDto toDto(OrderItem orderItem,
        Map<Long, List<MemberCouponResponseDto>> couponResponseList) {
        OrderItemResponseDto orderItemResponseDto = new OrderItemResponseDto();
        Item item = orderItem.getItem();
        orderItemResponseDto.id = item.getId();
        orderItemResponseDto.name = item.getName();
        orderItemResponseDto.price = orderItem.getOrderPrice();
        orderItemResponseDto.count = orderItem.getCount();
        orderItemResponseDto.category = item.getCategory().getDisplayName();
        orderItemResponseDto.thumbnailImage = item.getThumbnailImage();
        List<MemberCouponResponseDto> couponResponseDtos = couponResponseList.get(item.getId());
        if (couponResponseDtos != null) {
            orderItemResponseDto.couponList = couponResponseDtos;
        }
        return orderItemResponseDto;
    }

    public static OrderItemResponseDto toDto(OrderItem orderItem) {
        OrderItemResponseDto orderItemResponseDto = new OrderItemResponseDto();
        Item item = orderItem.getItem();
        orderItemResponseDto.id = item.getId();
        orderItemResponseDto.name = item.getName();
        orderItemResponseDto.price = orderItem.getOrderPrice();
        orderItemResponseDto.count = orderItem.getCount();
        orderItemResponseDto.category = item.getCategory().getDisplayName();
        orderItemResponseDto.thumbnailImage = item.getThumbnailImage();
        return orderItemResponseDto;
    }
}
