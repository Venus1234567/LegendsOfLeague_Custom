package com.project.legendsofleague.domain.item.service;

import com.project.legendsofleague.domain.item.domain.Item;
import com.project.legendsofleague.domain.item.repository.ItemLockRepository;
import com.project.legendsofleague.domain.order.dto.OrderItemStockDto;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ItemStockFacade {

    private final ItemLockRepository itemLockRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decreaseStock(List<OrderItemStockDto> orderItemStockDtoList) {
        orderItemStockDtoList.sort(Comparator.comparing(dto -> dto.getItem().getId()));
        try{
            for (OrderItemStockDto orderItemStockDto : orderItemStockDtoList) {
                Item item = orderItemStockDto.getItem();
                itemLockRepository.getLock(item.getId().toString());
            }

            for (OrderItemStockDto orderItemStockDto : orderItemStockDtoList) {
                Item item = orderItemStockDto.getItem();
                item.removeStock(orderItemStockDto.getQuantity());
            }
        } finally {
            for (OrderItemStockDto orderItemStockDto : orderItemStockDtoList) {
                Item item = orderItemStockDto.getItem();
                itemLockRepository.releaseLock(item.getId().toString());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increaseStock(List<OrderItemStockDto> orderItemStockDtoList) {
        try{
            for (OrderItemStockDto orderItemStockDto : orderItemStockDtoList) {
                Item item = orderItemStockDto.getItem();
                itemLockRepository.getLock(item.getId().toString());
            }

            for (OrderItemStockDto orderItemStockDto : orderItemStockDtoList) {
                Item item = orderItemStockDto.getItem();
                item.addStock(orderItemStockDto.getQuantity());
            }
        } finally {
            for (OrderItemStockDto orderItemStockDto : orderItemStockDtoList) {
                Item item = orderItemStockDto.getItem();
                itemLockRepository.releaseLock(item.getId().toString());
            }
        }
    }
}
