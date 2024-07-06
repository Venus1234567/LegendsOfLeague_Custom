package com.project.legendsofleague.scheduler;

import com.project.legendsofleague.domain.item.domain.Item;
import com.project.legendsofleague.domain.item.service.ItemStockFacade;
import com.project.legendsofleague.domain.order.domain.OrderStatus;
import com.project.legendsofleague.domain.order.dto.OrderItemStockDto;
import com.project.legendsofleague.domain.order.repository.order.OrderRepository;
import com.project.legendsofleague.domain.purchase.domain.Purchase;
import com.project.legendsofleague.domain.purchase.domain.PurchaseStatus;
import com.project.legendsofleague.domain.purchase.repository.PurchaseRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCleanupScheduler {

    private final PurchaseRepository purchaseRepository;

    private final OrderRepository orderRepository;

    private final ItemStockFacade itemStockFacade;

    /**
     * 애플리케이션이 실행될 때 1시간마다 Order 엔티티의 status가 PENDING + 현재 시간으로부터 1시간 이상 흘렀으면 삭제하는 로직
     */
    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void cleanupPendingOrders() {
        log.info("start cleanup order");
        log.info("=========================");
        LocalDateTime currentTime = LocalDateTime.now();

        LocalDateTime oneHourAgo = currentTime.minusHours(1);
        List<Purchase> purchaseList = purchaseRepository.queryPurchaseByPurchaseStatus(
            PurchaseStatus.PENDING);

        Map<Item, Integer> itemStockMap = makeItemStockMap(purchaseList);
        List<OrderItemStockDto> orderItemStockDtoList = itemStockMap.entrySet().stream()
            .map(entry -> new OrderItemStockDto(entry.getKey(), entry.getValue()))
            .toList();

        itemStockFacade.increaseStock(orderItemStockDtoList);

        purchaseRepository.deleteAllByPurchaseDateBeforeAndPurchaseStatusIn(oneHourAgo,
            List.of(PurchaseStatus.PENDING, PurchaseStatus.CANCEL));

        orderRepository.deleteByOrderDateBeforeAndOrderStatusEquals(oneHourAgo,
            OrderStatus.PENDING);
        log.info("=========================");
        log.info("finish cleanup order");
    }

    private Map<Item, Integer> makeItemStockMap(List<Purchase> purchaseList) {
        Map<Item, Integer> itemStockMap = new HashMap<>();
        purchaseList.forEach(purchase -> {
            purchase.getOrder().getOrderItemList().forEach(orderItem -> {
                Item item = orderItem.getItem();
                Integer val = itemStockMap.computeIfAbsent(item, Item::getStock);
                itemStockMap.put(item, val + orderItem.getCount());
            });
        });
        return itemStockMap;
    }

}
